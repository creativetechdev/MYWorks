package com.lexisnexis.converter;

import java.util.Date;

public class UtilConstants {
	public static String DTD = "dtd";

	public static String currWorkingDIR = System.getProperty("user.dir");

	public static String DTDDirectory = Converter.currWorkingDIR + System.getProperty("file.separator") + "resources"
			+ System.getProperty("file.separator") + "dtd" + System.getProperty("file.separator");
	public static String xslInput = Converter.currWorkingDIR + System.getProperty("file.separator") + "resources"
			+ System.getProperty("file.separator") + "rosetta-output-di-validation.xsl";
	public static String result = Converter.currWorkingDIR + System.getProperty("file.separator") + "validationLogs";

	public static String DIErrorLog = Converter.currWorkingDIR + System.getProperty("file.separator") + "validationLogs"
			+ System.getProperty("file.separator") + "DIValidationLog.txt";

	public static String ImageErrorLog = Converter.currWorkingDIR + System.getProperty("file.separator")
			+ "validationLogs" + System.getProperty("file.separator") + "imageError.txt";

	public static String DTDValidationErrorLog = Converter.currWorkingDIR + System.getProperty("file.separator")
			+ "validationLogs" + System.getProperty("file.separator") + "DTDValidationErrorLog.txt";

	// Messages

	// UI Messages
	public static String UIMessage = "-----------DI and Image/pdf/Doc Validation Tool-----------\n";

	public static String UIInputXmlSourceDirectoryMessage = "Please Enter xml Source Directory";

	public static String UIInputImagesDirectoryMessage = "Please Enter Image Directory";

	public static String ChoiceMessage = "Please enter your choice! : \n\t1. DI validation \n\t2. Inlineobject Attachment Validation \n\t3. DTD Validation";

	public static String ChoiceErrorMessage = "Please enter a valid Choice!";

	public static String ToolRerunMessage = "\nWould you want to run the tool again!\nPlease choose Y/N : ";

	// DI Validation Messages

	public static String InvalidDirectoryMessage = "\n!Error - Please enter valid directory path";

	public static String DIValidationSuccessMessage = "\nAll files are valid";

	public static String DIValidationInvalidMessage = "\n!Error - The DI Validation Failed";

	public static String DIValidationInvalidLogMessage = "\nThe errors has been listed in \"DIerrors.txt\" at : \n";

	public static String DIValidationStartedMessage = "\n-------------DI Validation Started---------------\n";

	public static String DIValidationProcessCompleteMessage = "\nDI Validation process completed please find the logs at : ";

	// Image Validation Messages

	public static String ImageValidationSuccessMessage = "\nAll images/pdf/Doc attachments are available in the source directory";
	
	public static String ImageValidationSuccessMessageWithWarnings = "\nImages/pdf/Doc Validation Done With some issues";
	
	public static String ImageValidationSuccessMessageWithWarningsType = "\nSome attachments name having case issues \n";

	public static String ImageValidationInvalidMessage = "!Error - Image/pdf/Doc Attachment Validation Failed\n";

	public static String ImageValidatioStartedMessage = "\n-------------Image/pdf/Doc Validation Started---------------\n";

	public static String ImageListNotAvailableMessage = "\nThe errors has been listed in \"imageErrors.txt\" at : \n";

	public static String ImageListinErrorLog = new Date()
			+ "\n\nList of Images/pdf/Doc not available in the source directory\n";

	public static String ImageValidationProcessCompleteMessage = "\nImage/pdf/Doc validation Process completed successfully";

	// DTD Validation Messages

	public static String DTDValidatioStartedMessage = "\n-------------DTD Validation Started---------------\n";
	
	public static String DTDValidationSuccessMessage = "\nAll XML files are valid in the source directory : ";

	public static String DTDValidationInvalidMessage = "\n!Error - XML Validation Failed\n";

	public static String DTDErrorListLog = "\nThe invalid files are listed in \"DTDValidationErrorLog.txt\" at : ";
	
	public static String DTDValidationInvalidFilesList = new Date()
			+ "\n\nList of Invalid Files : \n";

	public static String ProcessCompleteMessage = "\nThe process completed";

}
