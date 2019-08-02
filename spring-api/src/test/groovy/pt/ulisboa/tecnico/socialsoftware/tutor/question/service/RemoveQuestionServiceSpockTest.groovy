package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import spock.lang.Specification

@DataJpaTest
class RemoveQuestionServiceSpockTest extends Specification {
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'

    @Autowired
    QuestionService questionService

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    OptionRepository optionRepository

    @Autowired
    ImageRepository imageRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    def question
    def optionOK
    def optionKO

    def setup() {
        given: "create a question"
        question = new Question()
        question.setContent(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)
        question.setActive(true)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        and: 'an image'
        def image = new Image()
        image.setUrl(URL)
        image.setWidth(20)
        imageRepository.save(image)
        question.setImage(image)
        and: 'two options'
        optionOK = new Option()
        optionOK.setContent(OPTION_CONTENT)
        optionOK.setCorrect(true)
        optionRepository.save(optionOK)
        def options = new ArrayList<>()
        options.add(optionOK)
        optionKO = new Option()
        optionKO.setContent(OPTION_CONTENT)
        optionKO.setCorrect(false)
        optionRepository.save(optionKO)
        options.add(optionKO)
        question.setOptions(options)
        questionRepository.save(question)
    }

    def "remove a question"() {
        when:
        questionService.remove(question.getId())

        then: "the question is remove"
        questionRepository.count() == 0L
        imageRepository.count() == 0L
        optionRepository.count() == 0L

    }

    def "remove a question used in a quiz"() {
        given: "a question with answers"
        def quizQuestion = new QuizQuestion()
        quizQuestionRepository.save(quizQuestion)
        question.addQuizQuestion(quizQuestion)

        when:
        questionService.remove(question.getId())

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getError() == TutorException.ExceptionError.QUESTION_IS_USED_IN_QUIZ
    }

    def "remove a question that has topics"() {
        // TODO: implement
        when:
        questionService.remove(question.getId())

        then:
        thrown(TutorException)
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }
    }

}