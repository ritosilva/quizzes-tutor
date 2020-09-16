package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.DiscussionEntry
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User

import java.sql.Timestamp
import java.time.LocalDateTime

@DataJpaTest
class GetDiscussionEntryTest extends SpockTest {
    def questionAnswer
    def user
    def quiz
    def quizAnswer
    def quizQuestion
    def course
    def courseExecution

    def setup() {
        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL)
        courseExecutionRepository.save(courseExecution)

        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, User.Role.STUDENT, true, false)
        user.addCourse(courseExecution)
        userRepository.save(user)
        user.setKey(user.getId())

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(courseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        def question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(course)
        questionRepository.save(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        questionAnswer = new QuestionAnswer()
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswerRepository.save(questionAnswer)

        def clarification = new Clarification()
        clarification.setQuestionAnswer(questionAnswer)
        clarification.setTitle(CLARIFICATION_1_TITLE)
        clarification.setUser(userRepository.findAll().get(0))
        clarificationRepository.save(clarification)
    }

    def 'get a DiscussionEntry' () {
        given: 'a DiscussionEntry'
        DiscussionEntry discussionEntry = new DiscussionEntry()
        discussionEntry.setId(DISCUSSION_ENTRY_1_ID)
        discussionEntry.setUser(userRepository.findAll().get(0))
        def clarification = clarificationRepository.findAll().get(0)
        discussionEntry.setClarification(clarification)
        discussionEntry.setMessage(DISCUSSION_1_MESSAGE)
        clarification.addDiscussionEntry(discussionEntry)
        discussionEntry.setClarification(clarification)

        when:
        def discussionEntries = clarificationService.getDiscussionEntries(clarification.getId())

        then:
        discussionEntries.size() == 1
        def disc = discussionEntries.get(0)
        disc.getId() == DISCUSSION_ENTRY_1_ID
        disc.getMessage() == DISCUSSION_1_MESSAGE
    }

    def 'A Clarification with no discussionEntry'() {
        given: 'A clarification'
        def clarification = clarificationRepository.findAll().get(0)

        when:
        def discussionEntries = clarificationService.getDiscussionEntries(clarification.getId())

        then: 'it is empty'
        discussionEntries.size() == 0
    }

    def 'get two DiscussionEntries, ordered by dateTime' () {

        given: 'a DiscussionEntry'
        def clarification = clarificationRepository.findAll().get(0)

        DiscussionEntry discussionEntry = new DiscussionEntry()
        discussionEntry.setId(DISCUSSION_ENTRY_1_ID)
        discussionEntry.setUser(userRepository.findAll().get(0))
        discussionEntry.setLocalDateTime(LocalDateTime.now())
        discussionEntry.setClarification(clarification)
        discussionEntry.setMessage(DISCUSSION_1_MESSAGE)
        clarification.addDiscussionEntry(discussionEntry)
        discussionEntry.setClarification(clarification)

        Thread.sleep(100);

        and: 'a DiscussionEntry'
        DiscussionEntry discussionEntry1 = new DiscussionEntry()
        discussionEntry1.setId(DISCUSSION_ENTRY_2_ID)
        discussionEntry1.setUser(userRepository.findAll().get(0))
        discussionEntry1.setLocalDateTime(LocalDateTime.now())
        discussionEntry1.setClarification(clarification)
        discussionEntry1.setMessage(DISCUSSION_2_MESSAGE)
        clarification.addDiscussionEntry(discussionEntry1)
        discussionEntry1.setClarification(clarification)

        when:
        def discussionEntries = clarificationService.getDiscussionEntries(clarification.getId())

        then:
        discussionEntries.size() == 2
        def disc = discussionEntries.get(0)
        def disc1 = discussionEntries.get(1)
        disc.getDateTime() < disc1.getDateTime()

        disc.getId() == DISCUSSION_ENTRY_1_ID
        disc.getMessage() == DISCUSSION_1_MESSAGE

        disc1.getId() == DISCUSSION_ENTRY_2_ID
        disc1.getMessage() == DISCUSSION_2_MESSAGE
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
