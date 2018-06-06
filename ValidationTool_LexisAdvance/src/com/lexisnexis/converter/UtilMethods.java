package com.lexisnexis.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.saxonica.config.ProfessionalConfiguration;

import net.sf.saxon.TransformerFactoryImpl;

public class UtilMethods {
	private static String imageDirectory;
	private static String xmlInput;
	private static boolean successFlag;
	private static Set<String> SourceImageList = new HashSet<>();
	private static ArrayList<String> SourceImageListLowerCase = new ArrayList<>();
	private static Set<String> attachmentsImageList = new HashSet<>();
	// private static String imageAttachmentPattern1 = "<inlineobject
	// attachment=\"ln-server\" filename=\"(.*?)\" type=\"image\"/>";
	// private static String imageAttachmentPattern2 = "<inlineobject
	// type=\"image\" attachment=\"ln-server\" filename=\"(.*?)\"/>";
	// private static String imageAttachmentPattern3 = "<inlineobject
	// type=\"image\" filename=\"(.*?)\" attachment=\"ln-server\" />";
	private static String imageAttachmentPattern4 = "<inlineobject .*?/>";
	private static String imageAttachmentPattern5 = "filename=\"(.*?)\"";
	private static String linkAttachmentPattern6 = "<link .*?>";
	static Set<String> invalid;
	static StringBuffer imageList;
	static File error = new File(UtilConstants.ImageErrorLog);
	static int count = 1;

	public static void transformXml() throws IOException

	{
		try {
			invalid = new HashSet<String>();
			imageList = new StringBuffer(UtilConstants.ImageListinErrorLog);
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			if (br != null) {

				logInfoMessage(UtilConstants.UIMessage + UtilConstants.ChoiceMessage, true);
				// logInfoMessage(UtilConstants.ChoiceMessage);
				String choice = br.readLine();
				logInfoMessage(choice, false);
				if (!choice.isEmpty() && choice.equals("1")) {
					try {

						logInfoMessage(UtilConstants.UIInputXmlSourceDirectoryMessage, true);
						xmlInput = br.readLine();
						logInfoMessage(xmlInput, false);
						checkDirectory(xmlInput.toString());
						validateDIWithXsl(xmlInput, UtilConstants.xslInput, UtilConstants.result);

						logInfoMessage(UtilConstants.DIValidationProcessCompleteMessage + UtilConstants.DIErrorLog,
								true);
						reRunTool();

					} catch (Exception e) {
						logInfoMessage(e.getMessage(), true);
					}
				} else if (!choice.isEmpty() && choice.equals("2")) {
					try {
						logInfoMessage(UtilConstants.UIInputXmlSourceDirectoryMessage, true);
						xmlInput = br.readLine();
						logInfoMessage(xmlInput, false);

						checkDirectory(xmlInput.toString());

						logInfoMessage(UtilConstants.UIInputImagesDirectoryMessage, true);
						imageDirectory = br.readLine();
						logInfoMessage(imageDirectory, false);

						checkDirectory(imageDirectory.toString());

						// get list of all images from the input xml
						// attachmentsImageList =
						// getImageAttachmentsList(xmlInput);

						// get list of all images from the source directory
						SourceImageList = getSourceImageList(imageDirectory);

						// Validate images are available in source directory
						// validateImageAttachments(attachmentsImageList,
						// SourceImageList);

						// get the list of attachments and validate with
						// reference to source directory
						getImageAttachmentsListOfFile(xmlInput);

						reRunTool();

					} catch (Exception e) {
						logInfoMessage(e.getMessage(), true);
					}
				} else if (!choice.isEmpty() && choice.equals("3")) {
					logInfoMessage(UtilConstants.UIInputXmlSourceDirectoryMessage, true);
					xmlInput = br.readLine();
					logInfoMessage(xmlInput, false);
					checkDirectory(xmlInput.toString());

					validateWithDTD(xmlInput);

					reRunTool();
				} else {
					logInfoMessage(UtilConstants.ChoiceErrorMessage, true);
				}
			}
		} catch (Exception e) {
			logInfoMessage(e.getMessage(), true);
		}
	}

	public static void validateDIWithXsl(String xmlInput, String xslInput, String result) throws IOException {

		try {
			logInfoMessage(UtilConstants.DIValidationStartedMessage, true);

			File inputFile[] = new File(xmlInput).listFiles();
			FileUtils.copyDirectory(new File(UtilConstants.DTDDirectory), new File(xmlInput));

			for (File file : inputFile) {
				if (file.getName().toLowerCase().endsWith(".xml")) {
					System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
					ProfessionalConfiguration config = new ProfessionalConfiguration();
					config.setExtensionElementNamespace("http://ns.saxonica.com/sql",
							"net.sf.saxon.option.sql.SQLElementFactory");
					TransformerFactoryImpl xmlTransformerFactory = new TransformerFactoryImpl(config);
					xmlTransformerFactory.setAttribute("http://saxon.sf.net/feature/recoveryPolicy", 0);

					File stylesheet = new File(xslInput);
					File result1 = new File(result);
					StreamSource stylesource = new StreamSource(stylesheet);
					StreamSource xmlsource = new StreamSource(file);
					Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesource);

					// Transform the document and store it in a file
					transformer.transform(xmlsource, new StreamResult(result1));

					logInfoMessage(file.toString(), true);
				}
			}
		} catch (TransformerException e) {
			logInfoMessage(e.getMessage(), true);
		} finally {
			deleteFiles(xmlInput, UtilConstants.DTD);
		}
	}

	public static void validateImageAttachments(Set<String> attachmentsImageList, Set<String> SourceImageList) {

		Set<String> invalid = new HashSet<String>();
		StringBuffer imageList = new StringBuffer(UtilConstants.ImageListinErrorLog);
		File error = new File(UtilConstants.ImageErrorLog);
		int count = 1;
		SourceImageListLowerCase = (ArrayList<String>) SourceImageList.stream().map(String::toLowerCase)
				.collect(Collectors.toList());
		try {
			logInfoMessage(UtilConstants.ImageValidatioStartedMessage, true);
			for (String s : attachmentsImageList) {
				if (SourceImageList.contains(s)) {
				} else {
					if (SourceImageListLowerCase.contains(s.toLowerCase())) {
						invalid.add(s);
						imageList.append("\n" + count++ + " " + s + " : Verify the case");
						successFlag = true;
					} else {
						invalid.add(s);
						imageList.append("\n" + count++ + " " + s);
					}

				}
			}
			if (invalid.isEmpty()) {
				logInfoMessage(UtilConstants.ImageValidationProcessCompleteMessage
						+ UtilConstants.ImageValidationSuccessMessage, true);
				// logInfoMessage(UtilConstants.ImageValidationSuccessMessage);
				invalid = null;
			} else {
				if (successFlag) {
					logInfoMessage(UtilConstants.ImageValidationSuccessMessageWithWarnings
							+ UtilConstants.ImageValidationSuccessMessageWithWarningsType
							+ UtilConstants.ImageListNotAvailableMessage + UtilConstants.ImageErrorLog, true);
				} else {
					logInfoMessage(UtilConstants.ImageValidationInvalidMessage
							+ UtilConstants.ImageListNotAvailableMessage + UtilConstants.ImageErrorLog, true);
				}
				for (String img : invalid) {
					logInfo(img);
				}
				FileUtils.writeStringToFile(error, imageList.toString());
			}
		} catch (Exception e) {
			logInfoMessage(e.getMessage(), true);
		}
	}

	public static void validateImageAttachmentsInFiles(Set<String> attachmentsImageList, Set<String> SourceImageList,
			String filename) {

		SourceImageListLowerCase = (ArrayList<String>) SourceImageList.stream().map(String::toLowerCase)
				.collect(Collectors.toList());
		try {
			// logInfoMessage(UtilConstants.ImageValidatioStartedMessage, true);
			for (String s : attachmentsImageList) {
				if (SourceImageList.contains(s)) {
				} else {
					if (SourceImageListLowerCase.contains(s.toLowerCase())) {
						invalid.add(s);
						imageList.append("\n" + count++ + " " + s + " : Verify the attachment case : " + filename);
						successFlag = true;
					} else {
						invalid.add(s);
						imageList.append("\n" + count++ + " " + s + " : Not Found : " + filename);
					}

				}
			}
		} catch (Exception e) {
			logInfoMessage(e.getMessage(), true);
		}
	}

	public static Set<String> getImageAttachmentsList(String xmlInput) {
		Set<String> attachmentsImageList1 = new HashSet<>();
		try {

			File inputFile[] = new File(xmlInput).listFiles();

			for (File file : inputFile) {
				if (file.getName().toLowerCase().endsWith(".xml")) {

					String fileString = FileUtils.readFileToString(file, "UTF-8");
					Pattern pattern = Pattern.compile(imageAttachmentPattern4);

					Matcher matcher = pattern.matcher(fileString);
					while (matcher.find()) {

						String img = matcher.group(0).toString();
						// System.out.println(img);
						Pattern pattern1 = null;
						Matcher matcher1 = null;
						pattern1 = Pattern.compile(imageAttachmentPattern5);

						matcher1 = pattern1.matcher(img);
						while (matcher1.find()) {
							String filename = matcher1.group(1).toString();
							// System.out.println(filename);
							String ext = filename.substring(filename.indexOf("."));
							// System.out.println(ext);
							if (ext.toLowerCase().endsWith(".jpg") || ext.toLowerCase().endsWith(".pdf")) {
								attachmentsImageList1.add(filename);
							}

						}

					}

					Pattern pattern2 = Pattern.compile(linkAttachmentPattern6);

					Matcher matcher2 = pattern2.matcher(fileString);
					while (matcher2.find()) {

						String img = matcher2.group(0).toString();
						// System.out.println(img);
						Pattern pattern1 = null;
						Matcher matcher1 = null;
						pattern1 = Pattern.compile(imageAttachmentPattern5);

						matcher1 = pattern1.matcher(img);
						while (matcher1.find()) {
							String filename = matcher1.group(1).toString();
							// System.out.println(filename);
							String ext = filename.substring(filename.indexOf("."));
							// System.out.println(ext);
							if (ext.toLowerCase().endsWith(".pdf") || ext.toLowerCase().endsWith(".doc")) {
								attachmentsImageList1.add(filename);
							}

						}

					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return attachmentsImageList1;
	}

	public static Set<String> getSourceImageList(String imageDirectory) {
		Set<String> SourceImageList1 = new HashSet<>();
		try {
			File inputFile[] = new File(imageDirectory).listFiles();
			for (File file : inputFile) {
				if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".pdf")
						|| file.getName().toLowerCase().endsWith(".doc")
						|| file.getName().toLowerCase().endsWith(".gif") ||file.getName().toLowerCase().endsWith(".tif") || file.getName().toLowerCase().endsWith(".png")) {
					if (!SourceImageList1.contains(file)) {
						SourceImageList1.add(file.getName());
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return SourceImageList1;
	}

	public static boolean validateWithDTD(String xmlInput) throws ParserConfigurationException, IOException {

		try {
			logInfoMessage(UtilConstants.DTDValidatioStartedMessage, true);
			File inputFile[] = new File(xmlInput).listFiles();
			FileUtils.copyDirectory(new File(UtilConstants.DTDDirectory), new File(xmlInput));
			Set<String> invalidFiles = new HashSet<>();
			StringBuffer invalidFilesList = new StringBuffer(UtilConstants.DTDValidationInvalidFilesList);
			File invalidLogFile = new File(UtilConstants.DTDValidationErrorLog);
			for (File file : inputFile) {
				if (file.getName().toLowerCase().endsWith(".xml")) {
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setValidating(true);
						factory.setNamespaceAware(false);

						DocumentBuilder builder = factory.newDocumentBuilder();
						builder.setErrorHandler(new ErrorHandler() {
							public void warning(SAXParseException e) throws SAXException {
								logInfoMessage("WARNING : " + e.getMessage(), true);
							}

							public void error(SAXParseException e) throws SAXException {
								logInfoMessage("ERROR : " + e.getMessage(), false);
								invalidFiles.add(file.toString());
								invalidFilesList.append("\n" + file.toString());
							}

							public void fatalError(SAXParseException e) throws SAXException {
								logInfoMessage("FATAL : " + e.getMessage(), true);
							}
						});

						logInfoMessage(file.toString(), true);
						builder.parse(new InputSource(file.toString()));

					} catch (SAXException se) {
						// return false;
						se.printStackTrace();
					}
				}
			}
			if (invalidFiles.isEmpty()) {
				logInfoMessage(UtilConstants.DTDValidationSuccessMessage + xmlInput, true);
			} else {
				FileUtils.writeStringToFile(invalidLogFile, invalidFilesList.toString());
				logInfoMessage(UtilConstants.DTDValidationInvalidMessage + UtilConstants.DTDErrorListLog
						+ UtilConstants.DTDValidationErrorLog, true);
			}
		} catch (Exception e) {

		} finally {
			deleteFiles(xmlInput, UtilConstants.DTD);
		}
		return false;
	}

	public static void getImageAttachmentsListOfFile(String xmlInput) {
		Set<String> attachmentsImageList1 = new HashSet<>();
		try {
			logInfoMessage(UtilConstants.ImageValidatioStartedMessage, true);
			File inputFile[] = new File(xmlInput).listFiles();

			for (File file : inputFile) {
				if (file.getName().toLowerCase().endsWith(".xml")) {

					String fileString = FileUtils.readFileToString(file, "UTF-8");
					Pattern pattern = Pattern.compile(imageAttachmentPattern4);

					Matcher matcher = pattern.matcher(fileString);
					while (matcher.find()) {

						String img = matcher.group(0).toString();
						// System.out.println(img);
						Pattern pattern1 = null;
						Matcher matcher1 = null;
						pattern1 = Pattern.compile(imageAttachmentPattern5);

						matcher1 = pattern1.matcher(img);
						while (matcher1.find()) {
							String filename = matcher1.group(1).toString();
							// System.out.println(filename);
							String ext = filename.substring(filename.indexOf("."));
							// System.out.println(ext);
							if (ext.toLowerCase().endsWith(".jpg") || ext.toLowerCase().endsWith(".pdf")
									|| ext.toLowerCase().endsWith(".doc") || ext.toLowerCase().endsWith(".gif") || ext.toLowerCase().endsWith(".tif") || ext.toLowerCase().endsWith(".png")) {
								attachmentsImageList1.add(filename);
							}

						}

					}

					Pattern pattern2 = Pattern.compile(linkAttachmentPattern6);

					Matcher matcher2 = pattern2.matcher(fileString);
					while (matcher2.find()) {

						String img = matcher2.group(0).toString();
						// System.out.println(img);
						Pattern pattern1 = null;
						Matcher matcher1 = null;
						pattern1 = Pattern.compile(imageAttachmentPattern5);

						matcher1 = pattern1.matcher(img);
						while (matcher1.find()) {
							String filename = matcher1.group(1).toString();
							// System.out.println(filename);
							String ext = filename.substring(filename.indexOf("."));
							// System.out.println(ext);
							if (ext.toLowerCase().endsWith(".pdf") || ext.toLowerCase().endsWith(".doc")
									|| ext.toLowerCase().endsWith(".jpg") || ext.toLowerCase().endsWith(".gif") || ext.toLowerCase().endsWith(".tif") || ext.toLowerCase().endsWith(".png")) {
								attachmentsImageList1.add(filename);
							}

						}

					}
				}

				if (!attachmentsImageList1.isEmpty()) {
					validateImageAttachmentsInFiles(attachmentsImageList1, SourceImageList, file.toString());
					attachmentsImageList1.clear();
				}
			}
			if (invalid.isEmpty()) {
				logInfoMessage(UtilConstants.ImageValidationProcessCompleteMessage
						+ UtilConstants.ImageValidationSuccessMessage, true);
				// logInfoMessage(UtilConstants.ImageValidationSuccessMessage);
				invalid = null;
			} else {
				if (successFlag) {
					logInfoMessage(UtilConstants.ImageValidationSuccessMessageWithWarnings
							+ UtilConstants.ImageValidationSuccessMessageWithWarningsType
							+ UtilConstants.ImageListNotAvailableMessage + UtilConstants.ImageErrorLog, true);
					successFlag = false;
				} else {
					logInfoMessage(UtilConstants.ImageValidationInvalidMessage
							+ UtilConstants.ImageListNotAvailableMessage + UtilConstants.ImageErrorLog, true);
				}
				for (String img : invalid) {
					logInfo(img);
				}
				FileUtils.writeStringToFile(error, imageList.toString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// return attachmentsImageList1;
	}

	public static Set<String> getSourceImageDirectoryList(String imageDirectory) {
		Set<String> SourceImageList1 = new HashSet<>();
		try {
			File inputFile[] = new File(imageDirectory).listFiles();
			for (File file : inputFile) {
				if (file.isDirectory()) {
					getSourceImageDirectoryList(file.toString());
				} else{
					getSourceImageList(file.toString());
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return SourceImageList1;
	}
	
	public static void logInfo(String message) {
		Converter.logger.info(message + "\n");
	}

	public static void logInfoMessage(String message, boolean console) {
		if (console) {
			System.out.println(message);
		}
		logInfo(message);
		/*
		 * logMessage = UtilConstants.UIMessage; System.out.println(logMessage);
		 * logInfo(logMessage);
		 */
	}

	public static File checkDirectory(String directoryPath) {
		File directoryToCheck = null;
		directoryToCheck = new File(directoryPath);
		if ((directoryToCheck.exists()) && (directoryToCheck.isDirectory())) {
			return directoryToCheck;
		} else {
			logInfoMessage(UtilConstants.InvalidDirectoryMessage, true);
			System.exit(0);
		}
		return null;
	}

	public static void deleteFiles(String directoryPath, String type) {
		File inputFile[] = new File(directoryPath).listFiles();
		for (File file : inputFile) {
			if (file.getName().toLowerCase().endsWith(type)) {
				FileUtils.deleteQuietly(file);
			}
		}
	}

	public static void reRunTool() throws IOException {
		logInfoMessage(UtilConstants.ToolRerunMessage, true);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String c = br.readLine();
		logInfoMessage(c, false);
		if (c.equalsIgnoreCase("y")) {
			SourceImageList.clear();
			attachmentsImageList.clear();
			transformXml();
		} else {
			logInfoMessage(UtilConstants.ProcessCompleteMessage, true);
		}
	}

}
