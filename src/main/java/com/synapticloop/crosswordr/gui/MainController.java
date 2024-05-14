package com.synapticloop.crosswordr.gui;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import com.synapticloop.crosswordr.util.*;
import com.synapticloop.crosswordr.exception.PuzzlrException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class MainController {
	@FXML protected ToggleSwitch toggleSwitchTheme;
	@FXML protected VBox vBoxGenerators;
	@FXML protected DatePicker datePickerStart;
	@FXML protected DatePicker datePickerEnd;
	@FXML protected Label labelGeneratingFor;
	@FXML protected Button buttonGenerate;
	@FXML protected Label labelOutputDirectory;

	private Stage stage;
	private File outputDirectory;

	@FXML protected void initialize() {

		// get the output directory
		String outputDirectoryString = Settings.getStringSetting(MainApplication.class, "outputDirectory", null);
		if (null == outputDirectoryString) {
			datePickerStart.setDisable(true);
		} else {
			outputDirectory = new File(outputDirectoryString);
			if (outputDirectory.exists() && outputDirectory.isDirectory() && outputDirectory.canWrite()) {
				datePickerStart.setDisable(false);
				labelOutputDirectory.setText(outputDirectoryString);
			} else {
				datePickerStart.setDisable(true);
			}
			setFieldEnabled();
		}

		datePickerStart.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (null == newValue) {
				datePickerEnd.setValue(null);
			}

			setFieldEnabled();
			updateGenerateText();
		});

		datePickerEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
			setFieldEnabled();
			updateGenerateText();
		});

		// add all the generators

		try {
			loadCrosswords();
		} catch (IOException ignored) {
		}
	}

	@FXML protected void generate() {
		puzzles.clear();
		GENERATED_FILES.clear();
		WANTED_SLUGS.clear();
		for (Node child : vBoxGenerators.getChildren()) {
			if (child instanceof HBox) {
				for (Node node : ((HBox) child).getChildren()) {
					if (node instanceof CheckBox) {
						if (((CheckBox) node).isSelected()) {
							WANTED_SLUGS.add(node.getId());
						}
					}
				}
			}
		}

		buttonGenerate.setDisable(true);

		Task<Void> task = new Task<Void>() {
			@Override protected Void call() throws Exception {
				try {
					generatePDFs();
				} catch (ParseException | IOException e) {
					throw new RuntimeException(e);
				}
				return null;
			}
		};

		task.setOnSucceeded((value) -> buttonGenerate.setDisable(false));

		new Thread(task).start();
	}

	private void updateGenerateText() {
		StringBuffer sb = new StringBuffer();
		LocalDate startDate = datePickerStart.getValue();
		if (startDate != null) {
			sb.append("Generating: ");
			Date from = Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			sb.append(new SimpleDateFormat("dd/MM/yyyy").format(from));
		}

		LocalDate endDate = datePickerEnd.getValue();
		if (endDate != null) {
			sb.append(" to ");
			Date to = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			sb.append(new SimpleDateFormat("dd/MM/yyyy").format(to));
		}

		labelGeneratingFor.setText(sb.toString());
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML protected void outputDirectoryClick() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select output directory");
		if (null != outputDirectory) {
			directoryChooser.setInitialDirectory(outputDirectory);
		}

		File outputDirectoryTemp = directoryChooser.showDialog(this.stage);

		if (null != outputDirectoryTemp) {
			labelOutputDirectory.setText(outputDirectoryTemp.getAbsolutePath());
			outputDirectory = outputDirectoryTemp;
			Settings.setSetting(MainApplication.class, "outputDirectory", outputDirectory.getAbsolutePath());
		}

		setFieldEnabled();
	}

	private void setFieldEnabled() {
		if (null == outputDirectory) {
			datePickerStart.setDisable(true);
			datePickerEnd.setDisable(true);
			buttonGenerate.setDisable(true);

			optionDate = "";
			optionRangeStart = "";
			optionRangeEnd = "";
		} else {
			datePickerEnd.setDisable(null == datePickerStart.getValue());
			buttonGenerate.setDisable(null == datePickerStart.getValue());

			if(datePickerStart.getValue() != null) {
				optionDate = FormatHelper.formatDate(datePickerStart, "yyyy-MM-dd");
				optionRangeStart = FormatHelper.formatDate(datePickerStart, "yyyy-MM-dd");
			}

			if(datePickerEnd.getValue() != null) {
				optionRangeEnd = FormatHelper.formatDate(datePickerEnd, "yyyy-MM-dd");
			}
		}


	}

	@FXML protected void clearStart() {
		datePickerStart.setValue(null);
	}

	@FXML protected void clearEnd() {
		datePickerEnd.setValue(null);
	}

	@FXML protected void exit() {
		Platform.exit();
	}

	@FXML protected void minimise() {
		stage.setIconified(true);
	}


	@FXML protected void switchTheme() {
		setDarkmode(toggleSwitchTheme.isSelected());
	}

	public void setDarkmode(boolean darkmode) {
		Settings.setSetting(MainApplication.class, "darkmode", darkmode);
		toggleSwitchTheme.setSelected(darkmode);

		if (darkmode) {
			Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
		} else {
			Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
		}
	}


	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	// slug to title hashmap
	public static final Map<String, String> SLUG_MAP = new LinkedHashMap<>();

	// command line argument
	private static final String CMD_DATE_FORMAT = "yyyyMMdd";

	private static final String PUZZLE_TYPE_DATE_DEFAULT = "date";
	private static final String PUZZLE_TYPE_NUMBER = "number";

	// all the keys for the JSON parser
	private static final String JSON_KEY_PUZZLES = "puzzles";

	private static final String JSON_KEY_NAME = "name";
	private static final String JSON_KEY_SLUG = "slug";
	private static final String JSON_KEY_URL_FORMAT = "url_format";
	private static final String JSON_KEY_EXTRACTOR = "extractor";
	private static final String JSON_KEY_XSL = "xsl";
	private static final String JSON_KEY_TYPE = "type";
	private static final String JSON_KEY_TRANSLATE_DATE = "translateDate";
	private static final String JSON_KEY_TRANSLATE_NUMBER = "translateNumber";

	public static List<Puzzle> puzzles = new ArrayList<Puzzle>();

	public static List<String> GENERATED_FILES = new ArrayList<String>();

	private static JSONObject puzzlrJsonObject = null;

	// the default command line arguments
	public static String optionDate = SIMPLE_DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	public static String optionRangeStart = SIMPLE_DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	public static String optionRangeEnd = SIMPLE_DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	public static String optionSlugs = "";
	public static final Set<String> WANTED_SLUGS = new HashSet<>();


	public void generatePDFs() throws ParseException, IOException {
		// at this point in time we are going to go through the range start and end
		boolean shouldStop = false;

		optionDate = optionRangeStart;
		while (!shouldStop) {
			generatePuzzles(puzzlrJsonObject);
			if (optionRangeEnd.equals(optionDate)) {
				shouldStop = true;
			} else {
				// increment defaultCommandDate
				Date downloadDate = SIMPLE_DATE_FORMAT.parse(optionDate);
				Calendar instance = Calendar.getInstance();
				instance.setTime(downloadDate);
				instance.add(Calendar.DAY_OF_MONTH, 1);
				optionDate = SIMPLE_DATE_FORMAT.format(instance.getTime());
			}
		}

		writeXmlFilesAndMerge();

		PDFHelper.mergeFiles(outputDirectory, GENERATED_FILES, optionRangeStart, optionRangeEnd);
	}

	private void generatePuzzles(JSONObject puzzlrJsonObject) throws ParseException {
		Iterator<Object> puzzlesArrayIterator;
		puzzlesArrayIterator = puzzlrJsonObject.getJSONArray(JSON_KEY_PUZZLES).iterator();
		while (puzzlesArrayIterator.hasNext()) {
			Integer puzzleNumber = null;
			JSONObject puzzleObject = (JSONObject) puzzlesArrayIterator.next();

			String puzzleType = puzzleObject.optString(JSON_KEY_TYPE, PUZZLE_TYPE_DATE_DEFAULT);
			String urlFormat = puzzleObject.getString(JSON_KEY_URL_FORMAT);

			if (puzzleType.equalsIgnoreCase(PUZZLE_TYPE_DATE_DEFAULT)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(urlFormat);
				String formattedUrl = simpleDateFormat.format(SIMPLE_DATE_FORMAT.parse(optionDate));

				// if the wanted slugs set is empty - we want all, if not empty, only those slugs

				Puzzle puzzle = new Puzzle(
						puzzleObject.getString(JSON_KEY_NAME),
						puzzleObject.optString(JSON_KEY_SLUG, null),
						optionDate,
						formattedUrl,
						puzzleObject.getString(JSON_KEY_EXTRACTOR),
						puzzleObject.getString(JSON_KEY_XSL),
						puzzleType,
						optionDate,
						"-1"
				);
				if (WANTED_SLUGS.isEmpty() || WANTED_SLUGS.contains(puzzle.getSlug())) {
					puzzles.add(puzzle);
				}

			} else if (puzzleType.equalsIgnoreCase(PUZZLE_TYPE_NUMBER)) {
				// add a numeric format now
				String translateDate = puzzleObject.getString(JSON_KEY_TRANSLATE_DATE);
				String translateNumber = puzzleObject.getString(JSON_KEY_TRANSLATE_NUMBER);
				Date dateTranslate = new SimpleDateFormat(CMD_DATE_FORMAT).parse(translateDate);

				int numDaysDifference = (int) ((SIMPLE_DATE_FORMAT.parse(optionDate).getTime() - dateTranslate.getTime()) / (1000 * 60 * 60 * 24));
				int parseInt = Integer.parseInt(translateNumber);

				puzzleNumber = parseInt + numDaysDifference;
				String formattedUrl = String.format(urlFormat, puzzleNumber);
				Puzzle puzzle = new Puzzle(
						puzzleObject.getString(JSON_KEY_NAME),
						puzzleObject.getString(JSON_KEY_SLUG),
						optionDate,
						formattedUrl,
						puzzleObject.getString(JSON_KEY_EXTRACTOR),
						puzzleObject.getString(JSON_KEY_XSL),
						puzzleType,
						translateDate,
						translateNumber
				);
				puzzle.setPuzzleNumber(puzzleNumber);

				// if the wanted slugs set is empty - we want all, if not empty, only those slugs
				if (WANTED_SLUGS.isEmpty() || WANTED_SLUGS.contains(puzzle.getSlug())) {
					puzzles.add(puzzle);
				}
			} else {
				LOGGER.error("Unknown puzzle type of '{}'", puzzleType);
			}
		}

		// check to see whether we have a duplicate URL
		boolean hasDuplicate = false;
		Set<String> urls = new HashSet<String>();
		for (Puzzle puzzle : puzzles) {
			String formattedUrl = puzzle.getFormattedUrl();
			if (urls.contains(formattedUrl)) {
				LOGGER.error("Puzzle already contains url '{}'", formattedUrl);
				hasDuplicate = true;
			}
			urls.add(formattedUrl);
		}

		if (hasDuplicate) {
			LOGGER.error("Found duplicate urls for puzzles, exiting...");
			System.exit(-1);
		}
	}

	private void loadCrosswords() throws IOException {
		String puzzlrJson = IOUtils.toString(Objects.requireNonNull(MainController.class.getResourceAsStream("/crosswordr.json")), Charset.defaultCharset());
		puzzlrJsonObject = new JSONObject(puzzlrJson);
		// now let us print out the slugs
		for (Object o : puzzlrJsonObject.getJSONArray(JSON_KEY_PUZZLES)) {
			JSONObject puzzleObject = (JSONObject) o;
			String slug = puzzleObject.optString(JSON_KEY_SLUG, null);
			String puzzleName = puzzleObject.getString(JSON_KEY_NAME);
			if (null == slug) {
				LOGGER.warn("No slug found for puzzle named '{}', you WILL NOT be able to reference this puzzle", puzzleName);
			} else {
				LOGGER.info("Found slug '{}' for puzzle named '{}'", slug, puzzleName);
				if (SLUG_MAP.containsKey(slug)) {
					LOGGER.error("Slug '{}' already exists for puzzle named '{}', so the puzzle named '{}' will be ignored.", slug, SLUG_MAP.get(slug), puzzleName);
				} else {
					SLUG_MAP.put(slug, puzzleName);

					HBox hBox = new HBox();
					CheckBox checkBox = new CheckBox();
					checkBox.setId(slug);
					hBox.setSpacing(8.0);
					hBox.getChildren().add(checkBox);
					Label label = new Label(puzzleName);
					hBox.getChildren().add(label);
					vBoxGenerators.getChildren().add(hBox);
				}
			}
		}
	}

	/**
	 * Write out the XML file into the location
	 *
	 * @throws IOException if there was an error writing the file
	 */
	private void writeXmlFilesAndMerge() throws IOException {
		for (Puzzle puzzle : puzzles) {
			String xmlFileName = Constants.APP_CACHE_DIRECTORY + File.separator + puzzle.getFileName() + puzzle.getDate() + ".xml";
			File xmlFile = new File(xmlFileName);
			if (!xmlFile.exists()) {
				LOGGER.info("Downloading file '{}'", xmlFileName);
				try {
					FileUtils.writeStringToFile(xmlFile, puzzle.getData(), Charset.defaultCharset());
				} catch (PuzzlrException ex) {
					puzzle.setIsCorrect(false);
					LOGGER.error(ex.getMessage());
					continue;
				}
			} else {
				LOGGER.info("File exists, not downloading file '{}'", xmlFileName);
			}
			String pdfFile = PDFHelper.convertToPDF(xmlFile, outputDirectory, puzzle, puzzle.getPuzzleNumber());
			if (null != pdfFile) {
				GENERATED_FILES.add(pdfFile);
			}
		}
	}
}