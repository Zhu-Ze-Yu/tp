package commands;

import access.Access;
import exception.IncorrectAccessLevelException;
import exception.InvalidInputException;
import manager.admin.ModuleList;
import manager.card.Card;
import manager.chapter.CardList;
import manager.chapter.Chapter;
import manager.module.ChapterList;
import manager.module.Module;
import storage.Storage;
import ui.Ui;

import java.io.IOException;

import static common.Messages.MESSAGE_INVALID_ACCESS;

public class EditCommand extends Command {
    public static final String COMMAND_WORD = "edit";

    public static final String MODULE_PARAMETERS = " MODULE_NUMBER MODULE_NAME";
    public static final String MODULE_MESSAGE_USAGE = COMMAND_WORD + ": Edits the module name.\n"
            + "Parameters:" + MODULE_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " 1 CS2113T\n";

    public static final String CHAPTER_PARAMETERS = " CHAPTER_NUMBER CHAPTER_NAME";
    public static final String CHAPTER_MESSAGE_USAGE = COMMAND_WORD + ": Edits the chapter name.\n"
            + "Parameters:" + CHAPTER_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " 2 Chapter 2\n";

    public static final String CARD_PARAMETERS = " FLASHCARD_NUMBER q:QUESTION | a:ANSWER";
    public static final String CARD_MESSAGE_USAGE = COMMAND_WORD + ": Edits the flashcard content.\n"
            + "Parameters:" + CARD_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " 3 q:What is the result of one plus one | a:two\n";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edit the module name / chapter name / flashcard content.\n"
            + "Parameters:" + MODULE_PARAMETERS + "\n"
            + "           " + CHAPTER_PARAMETERS + "\n"
            + "           " + CARD_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " 1 CS2113T\n"
            + "         " + COMMAND_WORD + " 2 Chapter 2\n"
            + "         " + COMMAND_WORD + " 3 q:What is the result of one plus one | a:two\n";

    public static final String MESSAGE_BEFORE_EDIT = "The following %1$s will be edited:\n";
    public static final String MESSAGE_AFTER_EDIT = "Edited %1$s:\n";

    private static final String MODULE = "module";
    private static final String CHAPTER = "chapter";
    private static final String CARD = "card";

    private final int editIndex;
    private String moduleOrChapter;
    private String question;
    private String answer;
    private final String accessLevel;

    public EditCommand(int editIndex, String moduleOrChapter, String accessLevel) {
        this.editIndex = editIndex;
        this.moduleOrChapter = moduleOrChapter;
        this.accessLevel = accessLevel;
    }

    public EditCommand(int editIndex, String question, String answer, String accessLevel) {
        this.editIndex = editIndex;
        this.question = question;
        this.answer = answer;
        this.accessLevel = accessLevel;
    }

    @Override
    public void execute(Ui ui, Access access, Storage storage)
            throws InvalidInputException, IncorrectAccessLevelException, IOException {
        if (access.isChapterLevel()) {
            String result = editCard(access, storage);
            ui.showToUser(result);
        } else if (access.isAdminLevel()) {
            String result = editModule(access, storage);
            ui.showToUser(result);
        } else if (access.isModuleLevel()) {
            String result = editChapter(ui, access, storage);
            ui.showToUser(result);
        } else {
            throw new IncorrectAccessLevelException(String.format(MESSAGE_INVALID_ACCESS,
                    access.getLevel(), accessLevel));
        }
    }

    private String editCard(Access access, Storage storage) throws InvalidInputException, IOException {
        assert access.isChapterLevel() : "Not chapter level";
        //assert question.isEmpty() && answer.isEmpty() : "The content for question and answer are both empty.";
        CardList cards = access.getChapter().getCards();
        try {
            Card card = cards.getCard(editIndex);
            StringBuilder result = new StringBuilder();
            result.append(String.format(MESSAGE_BEFORE_EDIT, CARD));
            result.append(card.toString()).append("\n");
            if (!(question.isEmpty())) {
                card.setQuestion(question);
            }
            if (!(answer.isEmpty())) {
                card.setAnswer(answer);
            }
            result.append(String.format(MESSAGE_AFTER_EDIT, CARD));
            result.append(card.toString());
            storage.saveCards(cards, access.getModule().getModuleName(), access.getChapter().getChapterName());
            return result.toString();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            throw new InvalidInputException("The flashcard number needs to be within the range "
                    + "of the total number of flashcards\n");
        }
    }

    private String editModule(Access access, Storage storage) throws InvalidInputException {
        assert access.isAdminLevel() : "Not admin level";
        //assert moduleOrChapter.isEmpty() : "The module name is missing.";
        ModuleList modules = access.getAdmin().getModules();
        try {
            Module module = modules.getModule(editIndex);
            boolean success = storage.renameModule(moduleOrChapter, module);
            StringBuilder result = new StringBuilder();
            if (success) {
                result.append(String.format(MESSAGE_BEFORE_EDIT, MODULE));
                result.append(module.toString()).append("\n");
                module.setModuleName(moduleOrChapter);
                result.append(String.format(MESSAGE_AFTER_EDIT, MODULE));
                result.append(module.toString());
            }
            return result.toString();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            throw new InvalidInputException("The module number needs to be within the range "
                    + "of the total number of modules\n");
        }
    }

    private String editChapter(Ui ui, Access access, Storage storage) throws InvalidInputException {
        assert access.isModuleLevel() : "Not module level";
        //assert moduleOrChapter.isEmpty() : "The chapter name is missing.";
        ChapterList chapters = access.getModule().getChapters();
        try {
            Chapter chapter = chapters.getChapter(editIndex);
            boolean success = storage.renameChapter(moduleOrChapter, access, chapter);
            StringBuilder result = new StringBuilder();
            if (success) {
                result.append(String.format(MESSAGE_BEFORE_EDIT, CHAPTER));
                result.append(chapter.toString()).append("\n");
                chapter.setChapterName(moduleOrChapter);
                result.append(String.format(MESSAGE_AFTER_EDIT, CHAPTER));
                result.append(chapter.toString());
            }
            return result.toString();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            throw new InvalidInputException("The chapter number needs to be within the range "
                    + "of the total number of chapters\n");
        }
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
