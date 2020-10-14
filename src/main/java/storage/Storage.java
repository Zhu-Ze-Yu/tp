package storage;

import exception.InvalidFileFormatException;
import manager.card.Card;
import manager.chapter.CardList;
import manager.chapter.Chapter;
import parser.Parser;
import manager.module.Module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class Storage {
    public static final String FILE_PATHWAY = "questions.txt";   // file pathway

    public static final String QUESTION_ANSWER_PREFIX = " \\| ";
    public static final String QUESTION_PREFIX = "[Q]";
    public static final String ANSWER_PREFIX = "[A]";

    protected String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    //create the folder --> 'data/admin'
    public void createAdmin() {
        File f = new File(filePath);
        System.out.println("Filepath: " + filePath);

        boolean dataDirExists = f.getParentFile().exists();
        boolean dataDirCreated = false;
        if (!dataDirExists) {
            dataDirCreated = f.getParentFile().mkdir();
        } else {
            System.out.println("Directory " + f.getParentFile().getName() + " already exists");
        }

        if (dataDirCreated) {
            System.out.println("Successfully created new directory " + f.getParentFile().getName());
        }

        boolean adminDirExists = f.exists();
        boolean adminDirCreated = false;
        if (!adminDirExists) {
            adminDirCreated = f.mkdir();
        } else {
            System.out.println("Directory " + f + " already exists");
        }
        if (adminDirCreated) {
            System.out.println("Successfully created new directory " + f);
        }
    }

    public void createModule(String moduleName) {
        File f = new File(filePath + "/" + moduleName);
        boolean moduleDirExists = f.exists();
        boolean moduleDirCreated = false;
        if (!moduleDirExists) {
            moduleDirCreated = f.mkdir();
        } else {
            System.out.println("    Directory " + f + " already exists");
        }
        if (moduleDirCreated) {
            System.out.println("    Successfully created new directory " + f);
        }
    }

    public void createChapter(String chapterName, String moduleName) {
        try {
            File f = new File(filePath + "/" + moduleName + "/" + chapterName + ".txt");
            boolean chapterFileExists = f.exists();
            boolean chapterFileCreated = false;
            if (!chapterFileExists) {
                chapterFileCreated = f.createNewFile();
            } else {
                System.out.println("    File " + f + " already exists");
            }
            if (chapterFileCreated) {
                System.out.println("    Successfully created new file " + chapterName + ".txt");
            }
        } catch (IOException e) {
            System.out.println("Error creating the file.");
        }
    }

    public ArrayList<Module> loadModule() throws FileNotFoundException {
        File f = new File(filePath);
        boolean dirExists = f.exists();
        if (!dirExists) {
            throw new FileNotFoundException();
        }

        ArrayList<Module> modules = new ArrayList<>();
        String[] contents = f.list();
        System.out.println("List of files and directories in the specified directory:");
        for (int i = 0; i < contents.length; i++) {
            System.out.println(contents[i]);
            modules.add(new Module(contents[i]));
        }
        return modules;
    }

    public ArrayList<Chapter> loadChapter(String module) throws FileNotFoundException {
        File f = new File(filePath + "/" + module);
        boolean dirExists = f.exists();
        if (!dirExists) {
            throw new FileNotFoundException();
        }

        ArrayList<Chapter> chapters = new ArrayList<>();
        String[] contents = f.list();
        if (contents.length == 0) {
            return chapters;
        }
        System.out.println("List of files and directories in the specified directory:");
        for (int i = 0; i < contents.length; i++) {
            String target = contents[i].replace(".txt", "");
            System.out.println(contents[i]);
            chapters.add(new Chapter(target));
        }
        return chapters;
    }

    public ArrayList<Card> loadCard(String module, String chapter) throws FileNotFoundException, InvalidFileFormatException {
        File f = new File(filePath + "/" + module + "/" + chapter + ".txt");
        boolean fileExists = f.exists();
        if (!fileExists) {
            throw new FileNotFoundException();
        }

        ArrayList<Card> cards = new ArrayList<>();
        Scanner s = new Scanner(f);
        while (s.hasNext()) {
            //to read the card
            String fileCommand = s.nextLine();
            String[] args = fileCommand.split(QUESTION_ANSWER_PREFIX, 2);
            String question = Parser.parseQuestioninFile(args[0]);
            String answer = Parser.parseAnswerinFile(args[1]);
            Card card = new Card(question, answer);
            cards.add(card);
        }
        return cards;
    }

    public void saveCards(CardList cards, String module, String chapter) throws IOException {
        FileWriter fw = new FileWriter(filePath + "/" + module + "/" + chapter + ".txt");
        for (int i = 0; i < cards.getCardCount(); i++) {
            fw.write(cards.getCard(i).toString() + "\n");
        }
        fw.close();
    }
}
