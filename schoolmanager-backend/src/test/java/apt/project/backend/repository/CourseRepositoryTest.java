package apt.project.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import apt.project.backend.domain.Course;

public class CourseRepositoryTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private CourseRepository courseRepository;

    @BeforeClass
    public static void setUpClass() throws Exception {
        entityManagerFactory = Persistence.createEntityManagerFactory("H2");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        entityManagerFactory.close();
    }

    @Before
    public void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createNativeQuery("TRUNCATE TABLE public.Course")
                .executeUpdate();
        entityManager.getTransaction().commit();
        courseRepository = new CourseRepository(entityManager);

    }

    @After
    public void tearDown() {
        entityManager.clear();
//        if ( entityManager.getTransaction().isActive() ) {
//            entityManager.getTransaction().rollback();
//        }
        entityManager.close();
    }

    @Test
    public void testFindAllWhenDataBaseIsEmpty() {
        // exercise and verify
        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    public void testFindAllWhenDataBaseIsNotEmpty() {
        // setUp
        Course course1 = new Course("Course1");
        Course course2 = new Course("Course2");
        entityManager.getTransaction().begin();
        entityManager.persist(course1);
        entityManager.persist(course2);
        entityManager.getTransaction().commit();
        // exercise and verify
        assertThat(courseRepository.findAll()).containsExactly(course1,
                course2);
    }

    @Test
    public void testFindByTitleWhenCourseExist() {
        // setUp
        Course course1 = new Course("Course1");
        Course course2 = new Course("Course2");
        entityManager.getTransaction().begin();
        entityManager.persist(course1);
        entityManager.persist(course2);
        entityManager.getTransaction().commit();
        // exercise and verify
        assertThat(courseRepository.findByTitle("Course1")).isEqualTo(course1);
    }

    @Test
    public void testFindByTitleWhenTheCourseDoesNotExist() {
        // exercise and verify
        assertThat(courseRepository.findByTitle("Course1")).isNull();
    }

    @Test
    public void testSave() {
        // setup
        Course course = new Course("Course1");
        // exercise
        courseRepository.save(course);
        // verify
        entityManager.getTransaction().begin();
        List<Course> courses = entityManager
                .createQuery("from Course", Course.class).getResultList();
        entityManager.getTransaction().commit();
        assertThat(courses).containsExactly(course);
    }

    @Test
    public void testDelete() {
        // setup
        Course courseToDelete = new Course("Course1");
        entityManager.getTransaction().begin();
        entityManager.persist(courseToDelete);
        entityManager.getTransaction().commit();
        // exercise
        courseRepository.delete(courseToDelete);
        // verify
        entityManager.getTransaction().begin();
        List<Course> courses = entityManager
                .createQuery("from Course", Course.class).getResultList();
        entityManager.getTransaction().commit();
        assertThat(courses).isEmpty();
    }

    @Test
    public void testUpdate() {
        // setup
        Course existingCourse = new Course("Course1");
        entityManager.getTransaction().begin();
        entityManager.persist(existingCourse);
        entityManager.getTransaction().commit();
        entityManager.clear();
        existingCourse.setTitle("Course1Modified");
        // exercise
        courseRepository.update(existingCourse);
        // verify
        entityManager.getTransaction().begin();
        Course retrievedCourse = entityManager.find(Course.class,
                existingCourse.getId());
        entityManager.getTransaction().commit();

        assertThat(retrievedCourse)
                .isEqualToComparingFieldByField(existingCourse);
    }
}