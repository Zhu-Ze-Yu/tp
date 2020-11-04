package commands;

import access.Access;
import common.KajiLog;
import exception.InvalidInputException;
import manager.card.Card;
import manager.chapter.CardList;
import storage.Storage;
import ui.Ui;

import java.io.IOException;
import java.util.logging.Logger;

import static common.Messages.CARD;
import static common.Messages.MESSAGE_INVALID_INDEX_RANGE;
import static common.Messages.MESSAGE_ITEM_EXISTED;

public class EditCardCommand extends EditCommand {
    private static Logger logger = KajiLog.getLogger(EditCardCommand.class.getName());

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the flashcard content.\n"
            + "Parameters:" + CARD_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " 3 q:What is the result of one plus one | a:two\n";

    private static final String MESSAGE_SAME_QUESTION = "%1$s has the same question as what you entered: %2$s\n";
    private static final String MESSAGE_SAME_ANSWER = "%1$s has the same answer as what you entered: %2$s\n";
    private static final String MESSAGE_SAME_CONTENT = "%1$s has the same content as what you entered: "
            + "[Q] %2$s | [A] %3$s\n";

    private final int editIndex;
    private String question;
    private String answer;

    public EditCardCommand(int editIndex, String question, String answer) {
        this.editIndex = editIndex;
        this.question = question;
        this.answer = answer;
    }

    @Override
    public void execute(Ui ui, Access access, Storage storage) throws InvalidInputException, IOException {
        String result = editCard(access, storage);
        ui.showToUser(result);
    }

    private String editCard(Access access, Storage storage) throws InvalidInputException, IOException {
        assert access.isChapterLevel() : "Not chapter level";
        assert !question.isEmpty() || !answer.isEmpty() : "The content for question and answer are both empty.";
        CardList cards = access.getChapter().getCards();
        try {
            Card card = cards.getCard(editIndex);

            String checkResult = checkCardContent(card);
            if (!checkResult.isEmpty()) {
                return checkResult;
            }

            if (question.isEmpty()) {
                question = card.getQuestion();
            }
            if (answer.isEmpty()) {
                answer = card.getAnswer();
            }

            if (cards.checkCardExistence(question.toLowerCase(), answer.toLowerCase())
                    && !(question.toLowerCase().equals(card.getQuestion().toLowerCase())
                    && answer.toLowerCase().equals(card.getAnswer().toLowerCase()))) {
                return String.format(MESSAGE_ITEM_EXISTED, CARD, "[Q] " + question + " [A] " + answer, CARD);
            }

            logger.info("Editing flashcard: " + card);
            StringBuilder result = new StringBuilder();
            result.append(prepareBeforeEdit(CARD, card.toString()));
            card.setQuestion(question);
            card.setAnswer(answer);
            logger.info("Flashcard successfully edited to: " + card);
            result.append(prepareAfterEdit(CARD, card.toString()));
            storage.saveCards(cards, access.getModule().getModuleName(), access.getChapter().getChapterName());
            return result.toString();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            throw new InvalidInputException(String.format(MESSAGE_INVALID_INDEX_RANGE, CARD));
        }
    }

    private String checkCardContent(Card card) {
        if (question.equals(card.getQuestion()) && answer.equals(card.getAnswer())) {
            return String.format(MESSAGE_SAME_CONTENT, card, question, answer);
        }
        if (answer.isEmpty() && question.equals(card.getQuestion())) {
            return String.format(MESSAGE_SAME_QUESTION, card, question);
        }
        if (question.isEmpty() && answer.equals(card.getAnswer())) {
            return String.format(MESSAGE_SAME_ANSWER, card, answer);
        }

        return "";
    }
}
