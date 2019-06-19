package edu.hyeokjaekwon.java;

import java.io.File; 
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Hjls{
	
	private boolean isFOption;
	private boolean isaOption;
	private boolean islOption;
	private boolean isfOption;
	private boolean ishOption;
	private boolean istOption;
	
	public void run(String[] args){
		
		CommandLineParser parser = new DefaultParser();

		Options options = createOptions();
		
		try{
			CommandLine cmd = parser.parse(options, args);
			
			isFOption = cmd.hasOption("F");
			isaOption = cmd.hasOption("a");
			islOption = cmd.hasOption("l");
			isfOption = cmd.hasOption("f");
			ishOption = cmd.hasOption("h");
			ishOption = cmd.hasOption("t");
			
			File pwd = new File(System.getProperty("user.dir"));
			
			if(cmd.getArgList().size() > 0)	
				pwd = new File(cmd.getArgList().get(0));
			
			printFileDirectory(pwd);
		}
		catch (ParseException e){
			printHelp(options);
			System.exit(-1);
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}
	/**
	 * @param file to print.
	 * @throws IOException
	 */
	private void printOneFile(File file) throws IOException{
		if (isFOption){
			
			if (file.isDirectory())	
				System.out.print("/");
			else if (Files.isSymbolicLink(file.toPath()))	
				System.out.print("@");
			else if (file.isFile())	
				System.out.print("*");
		}
		
		if (islOption){
			
			Path path = file.toPath();
			
			String type;
			
			if (file.isDirectory())	
				type = "d";
			else if (file.isFile())	
				type = "-";
			else if (Files.isSymbolicLink(file.toPath()))
				type = "l";
			else 
				type = " ";
			
			int linkCount = 0;
						
			String permission;
			String ownerName;
			String groupName;
			
			boolean isWindows = System.getProperty("os.name").startsWith("Windows");
			
			if (isWindows){
				
				AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
				ownerName = view.getOwner().getName();
				groupName = " ";
				permission = file.canRead()?"r":"-";
				permission += file.canExecute()?"x":"-";
				permission += file.canWrite()?"w":"-";
			}else{
				
				PosixFileAttributes posixAttr = Files.readAttributes(path, PosixFileAttributes.class);
				ownerName = posixAttr.owner().getName();
				groupName = posixAttr.group().getName();
				permission = PosixFilePermissions.toString(posixAttr.permissions());

				linkCount = (int)Files.getAttribute(path, "unix:nlink", NOFOLLOW_LINKS);
			}
			long fileSize = (long)Files.getAttribute(path, "basic:size", NOFOLLOW_LINKS);
			String fileSizeString = String.valueOf(fileSize);
			
			if (ishOption){
				fileSizeString = FileSize(fileSize);
			}
			
			FileTime fileTime = (FileTime)Files.getAttribute(path, "basic:lastModifiedTime", NOFOLLOW_LINKS);
		
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm yyyy", Locale.US);
			String lastFixedTime = dateFormat.format(new Date(fileTime.toMillis()));
			
			if (isWindows)
				System.out.print(String.format("%s%s%4d %10s %6s %7s %s %s", type, permission, linkCount, ownerName, groupName, fileSizeString, lastFixedTime, file.getName()));
			else
				System.out.print(String.format("%s%s %s %6s %7s %s %s", type, permission, ownerName, groupName, fileSizeString, lastFixedTime, file.getName()));
		}
		else
			System.out.print(file.getName());
		
		if (Files.isSymbolicLink(file.toPath()))
			System.out.print(" --> " + file.toPath().toRealPath().getFileName());
		
		System.out.println();
	}
	private String FileSize(long fileSize) {
		return null;
	}
	private void printFileDirectory(File directory) throws IOException{
		
		ArrayList<File> fileList = new ArrayList<File>();
		
		if (isaOption)
			fileList.addAll(Arrays.asList(directory.listFiles()));
		else	
			fileList.addAll(Arrays.asList(directory.listFiles((FileFilter)HiddenFileFilter.VISIBLE)));
		
		if (!isfOption){
			
			fileList.sort(new Comparator<File>() {
				public int compare(File o1, File o2){
					return o1.getName().compareTo(o2.getName());
				};
			});
		}
		if (islOption){
			System.out.println("total " + fileList.size());
		}
		if (isaOption){
			
			fileList.add(0, new File(".."));
			fileList.add(0, new File("."));
		}
		for (File f:fileList){
			printOneFile(f);
		}
	}

	private Options createOptions(){
		
		Options options = new Options();

		options.addOption(Option.builder("F")
				.desc("add same file character")
				.build());
		
		options.addOption(Option.builder("a")
				.desc("all array. '.', '..' like files")
				.build());
		
		options.addOption(Option.builder("l")
				.desc("long option")
				.build());

		options.addOption(Option.builder("f")
				.desc("no sorting")
				.build());
		
		options.addOption(Option.builder("h")
		        .desc("people can read")
		        .build());
		
		options.addOption(Option.builder("t")
		        .desc("sort by time")
		        .build());
		
		return options;
	}

	private void printHelp(Options options) {
		
		HelpFormatter formatter = new HelpFormatter();
		String header = "Hjls: implementation of ls command.";
		String footer = " ";
		formatter.printHelp("Hjls", header, options, footer, true);
	}
}
