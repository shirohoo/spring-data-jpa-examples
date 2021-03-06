package learn.jpa.repository;

import learn.jpa.model.Simple;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import static org.springframework.data.domain.ExampleMatcher.matching;
import static org.springframework.data.domain.Sort.Order;
import static org.springframework.data.domain.Sort.by;

@DataJpaTest
class SimpleRepositoryTest {
    private final SimpleRepository simpleRepository;
    private final TestEntityManager entityManager;

    public SimpleRepositoryTest(SimpleRepository simpleRepository, TestEntityManager entityManager) {
        this.simpleRepository = simpleRepository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUp() {
        List<Simple> Simples = List.of(Simple.createSimple("siro", 29),
                                       Simple.createSimple("sophia", 32),
                                       Simple.createSimple("dennis", 25),
                                       Simple.createSimple("james", 41),
                                       Simple.createSimple("michael", 33));

        simpleRepository.saveAllAndFlush(Simples);
    }

    @AfterEach
    void tearDown() {
        entityManager.getEntityManager()
                     .createNativeQuery("ALTER TABLE `SIMPLE` ALTER COLUMN `ID` RESTART WITH 1")
                     .executeUpdate();
    }

    private Sort orderByIdDesc() {
        return by(Order.desc("id"));
    }

    @Test
    @DisplayName("Simple_1??????_??????")
    void findById() {
        Simple Simple = simpleRepository.findById(1L)
                                        .orElseThrow(NoSuchElementException::new);
        assertThat(Simple.getName()).isEqualTo("siro");
        assertThat(Simple.getAge()).isEqualTo(29);
    }

    @Test
    @DisplayName("Simple_1???_3??????_??????")
    void findAllById() {
        List<Simple> Simples = simpleRepository.findAllById(Lists.newArrayList(1L, 3L));
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("dennis", 25))
                           .size().isEqualTo(2);
    }

    @Test
    @DisplayName("Simple_??????_????????????_5???")
    void findAll() {
        List<Simple> Simples = simpleRepository.findAll();
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("sophia", 32),
                                     tuple("dennis", 25),
                                     tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(5);
    }

    @Test
    @DisplayName("Simple_1??????_??????")
    void deleteById() {
        simpleRepository.deleteById(1L);
        List<Simple> Simples = simpleRepository.findAll();
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("sophia", 32),
                                     tuple("dennis", 25),
                                     tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(4);
    }

    @Test
    @DisplayName("Simple_1???_3??????_??????")
    void deleteAllById() {
        simpleRepository.deleteAllById(Lists.newArrayList(1L, 3L));
        List<Simple> Simples = simpleRepository.findAll();
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("sophia", 32),
                                     tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(3);
    }

    @Test
    @DisplayName("Simple_????????????")
    void deleteAll() {
        simpleRepository.deleteAll();
        List<Simple> Simples = simpleRepository.findAll();

        assertThat(Simples).isEmpty();
    }

    @Test
    @DisplayName("Simple_Batch_1???_3??????_??????")
    void deleteAllByIdInBatch() {
        simpleRepository.deleteAllByIdInBatch(Lists.newArrayList(1L, 3L));
        List<Simple> Simples = simpleRepository.findAll();
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("sophia", 32),
                                     tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(3);
    }

    @Test
    @DisplayName("Simple_Batch_????????????")
    void deleteAllInBatch() {
        simpleRepository.deleteAllInBatch();
        List<Simple> Simples = simpleRepository.findAll();
        assertThat(Simples).isEmpty();
    }

    @Test
    @DisplayName("Simple_1??????_???????????????_??????")
    void existsById() {
        boolean exists = simpleRepository.existsById(1L);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Simple_????????????_??????")
    void count() {
        long count = simpleRepository.count();
        assertThat(count).isEqualTo(5);
    }

    /**
     * JPA Page??? 0?????? ???????????? <br/>
     * <br/>
     * Creates a new unsorted {@link PageRequest}. <br/>
     * page zero-based page index, must not be negative. <br/>
     * the size of the page to be returned, must be greater than 0. <br/>
     * <br/>
     * ???????????? ?????? <br/>
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-0.png"
     */
    @Test
    @DisplayName("Page_API")
    void pageV1() {
        Page<Simple> Simples = simpleRepository.findAll(PageRequest.of(1, 3));
        Pageable pageable = Simples.getPageable();

        Sort sort = Simples.getSort();
        int pageNumber = pageable.getPageNumber();
        int totalPages = Simples.getTotalPages();
        long totalElements = Simples.getTotalElements();
        int numberOfElements = Simples.getNumberOfElements();
        int size = Simples.getSize();

        assertThat(sort.isUnsorted()).isTrue();
        assertThat(pageNumber).isEqualTo(1);
        assertThat(totalPages).isEqualTo(2);
        assertThat(totalElements).isEqualTo(5);
        assertThat(numberOfElements).isEqualTo(2);
        assertThat(size).isEqualTo(3);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(2);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-0.png"
     */
    @Test
    @DisplayName("Query_Methods_Pageable_??????")
    void pageV2() {
        List<Simple> createSimples = new ArrayList<>();
        createSimples.add(Simple.createSimple("siro", 11));
        createSimples.add(Simple.createSimple("siro", 22));
        createSimples.add(Simple.createSimple("siro", 33));
        createSimples.add(Simple.createSimple("siro", 44));
        simpleRepository.saveAllAndFlush(createSimples);

        Page<Simple> Simples = simpleRepository.findByName("siro", PageRequest.of(0, 3, orderByIdDesc()));
        Pageable pageable = Simples.getPageable();

        Sort sort = Simples.getSort();
        int pageNumber = pageable.getPageNumber();
        int totalPages = Simples.getTotalPages();
        long totalElements = Simples.getTotalElements();
        int numberOfElements = Simples.getNumberOfElements();
        int size = Simples.getSize();

        assertThat(sort.isSorted()).isTrue();
        assertThat(pageNumber).isEqualTo(0);
        assertThat(totalPages).isEqualTo(2);
        assertThat(totalElements).isEqualTo(5);
        assertThat(numberOfElements).isEqualTo(3);
        assertThat(size).isEqualTo(3);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 44),
                                     tuple("siro", 33),
                                     tuple("siro", 22))
                           .size().isEqualTo(3);

        Simples = simpleRepository.findByName("siro", PageRequest.of(1, 3, orderByIdDesc()));
        pageable = Simples.getPageable();

        sort = Simples.getSort();
        pageNumber = pageable.getPageNumber();
        totalPages = Simples.getTotalPages();
        totalElements = Simples.getTotalElements();
        numberOfElements = Simples.getNumberOfElements();
        size = Simples.getSize();

        assertThat(sort.isSorted()).isTrue();
        assertThat(pageNumber).isEqualTo(1);
        assertThat(totalPages).isEqualTo(2);
        assertThat(totalElements).isEqualTo(5);
        assertThat(numberOfElements).isEqualTo(2);
        assertThat(size).isEqualTo(3);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 11),
                                     tuple("siro", 29))
                           .size().isEqualTo(2);
    }

    @Test
    @DisplayName("Example_API")
    void exampleFindAll() {
        ExampleMatcher matcher = matching()
                .withIgnorePaths("age") // age ??? ???????????? ????????????
                .withMatcher("name", GenericPropertyMatchers.contains()); // name ??? ??????????????? ??????????????? - like ??????

        /*-----------------------------------------
         ?????? ????????? ?????? Simple proxy ??? ????????????
         name ??? i??? ???????????? ????????? ????????????
         age ??? ??????????????? ?????? ????????? ????????????
         -----------------------------------------*/
        Example<Simple> example = Example.of(Simple.createSimple("i", 0), matcher);

        List<Simple> Simples = simpleRepository.findAll(example);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("sophia", 32),
                                     tuple("dennis", 25),
                                     tuple("michael", 33))
                           .size().isEqualTo(4);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-1.png"
     */
    @Test
    @DisplayName("Query_Methods_??????_?????????")
    void queryMethodsV1() {
        Simple simple = Simple.createSimple("tester", 77);
        Simple tester = simpleRepository.save(simple);

        assertThat(tester).usingRecursiveComparison().isEqualTo(simpleRepository.findByName("tester"));
        assertThat(tester).usingRecursiveComparison().isEqualTo(simpleRepository.getByName("tester"));
        assertThat(tester).usingRecursiveComparison().isEqualTo(simpleRepository.readByName("tester"));
        assertThat(tester).usingRecursiveComparison().isEqualTo(simpleRepository.queryByName("tester"));
        assertThat(tester).usingRecursiveComparison().isEqualTo(simpleRepository.searchByName("tester"));
        assertThat(tester).usingRecursiveComparison().isEqualTo(simpleRepository.streamByName("tester"));
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-1.png"
     */
    @Test
    @DisplayName("Query_Methods_Top_??????")
    void queryMethodsV2() {
        /*-----------------------------------------
         id=1 siro ??? id=6 siro ??? ???????????? ????????????
        limit query ??? ???????????? id ??????????????? ??? ?????? ???????????? ????????????
         -----------------------------------------*/
        Simple simple = Simple.createSimple("siro", 77);
        simpleRepository.saveAndFlush(simple); // id=6 siro save

        Simple siro = simpleRepository.findById(1L).get(); // id=1 siro select
        assertThat(siro).usingRecursiveComparison().isEqualTo(simpleRepository.findTop1ByName("siro"));
        assertThat(siro).usingRecursiveComparison().isEqualTo(simpleRepository.findFirst1ByName("siro"));

        List<Simple> Simples = simpleRepository.findTop2ByName("siro"); //  limit = 2 select
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("siro", 77))
                           .size().isEqualTo(2);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_And_??????")
    void queryMethodsV3() {
        Simple simple = Simple.createSimple("siro", 77);
        simpleRepository.saveAndFlush(simple); // id=6 siro save

        Simple siro = simpleRepository.findByNameAndAge("siro", 77);
        assertThat(siro).extracting("name", "age")
                        .containsExactly("siro", 77);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_Or_??????")
    void queryMethodsV4() {
        Simple simple = Simple.createSimple("siro", 25);
        simpleRepository.saveAndFlush(simple); // id=6 siro save

        List<Simple> Simples = simpleRepository.findByNameOrAge("siro", 25);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("dennis", 25),
                                     tuple("siro", 25))
                           .size().isEqualTo(3);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_After_??????(??????)")
    void queryMethodsV5() {
        List<Simple> Simples = simpleRepository.findByIdAfter(1L);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("sophia", 32),
                                     tuple("dennis", 25),
                                     tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(4);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_After_??????(??????)")
    void queryMethodsV6() {
        List<Simple> Simples = simpleRepository.findByIdGreaterThanEqual(1L);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("sophia", 32),
                                     tuple("dennis", 25),
                                     tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(5);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_Before_??????(??????)")
    void queryMethodsV7() {
        List<Simple> Simples = simpleRepository.findByIdBefore(5L);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("sophia", 32),
                                     tuple("dennis", 25),
                                     tuple("james", 41))
                           .size().isEqualTo(4);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_Before_??????(??????)")
    void queryMethodsV8() {
        List<Simple> Simples = simpleRepository.findByIdIsLessThanEqual(5L);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("sophia", 32),
                                     tuple("dennis", 25),
                                     tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(5);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_Between_??????")
    void queryMethodsV9() {
        List<Simple> Simples = simpleRepository.findByAgeBetween(20, 30);
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("dennis", 25))
                           .size().isEqualTo(2);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_NotNull_??????")
    void queryMethodsV10() {
        List<Simple> Simples = simpleRepository.findByIdIsNotNull();
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("sophia", 32),
                                     tuple("dennis", 25),
                                     tuple("james", 41),
                                     tuple("michael", 33))
                           .size().isEqualTo(5);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_In_??????(Batch)")
    void queryMethodsV11() {
        List<Simple> Simples = simpleRepository.findByAgeIn(Lists.newArrayList(29, 32, 25));
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 29),
                                     tuple("sophia", 32),
                                     tuple("dennis", 25))
                           .size().isEqualTo(3);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_Starting_??????")
    void queryMethodsV12() {
        Simple siro = simpleRepository.findByNameStartingWith("si").get(0);
        assertThat(siro).extracting("name", "age")
                        .containsExactly("siro", 29);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_Ending_??????")
    void queryMethodsV13() {
        Simple siro = simpleRepository.findByNameEndingWith("ro").get(0);
        assertThat(siro).extracting("name", "age")
                        .containsExactly("siro", 29);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-2.png"
     */
    @Test
    @DisplayName("Query_Methods_Containing_??????")
    void queryMethodsV14() {
        Simple siro = simpleRepository.findByNameContaining("ir").get(0);
        assertThat(siro).extracting("name", "age")
                        .containsExactly("siro", 29);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-3.png"
     */
    @Test
    @DisplayName("Query_Methods_First_OrderBy_??????")
    void queryMethodsV15() {
        Simple simple = Simple.createSimple("siro", 77);
        simpleRepository.saveAndFlush(simple);

        List<Simple> Simples = simpleRepository.findFirst2ByNameOrderByIdDesc("siro");
        assertThat(Simples).extracting("name", "age")
                           .contains(tuple("siro", 77),
                                     tuple("siro", 29))
                           .size().isEqualTo(2);
    }

    /**
     * ???????????? ??????
     *
     * @see "Han-Changhun/src/test/resources/images/query-method-3.png"
     */
    @Test
    @DisplayName("Query_Methods_Sort_??????")
    void queryMethodsV16() {
        List<Simple> simple = simpleRepository.findAll(orderByIdDesc());
        assertThat(simple).extracting("name", "age")
                          .contains(tuple("michael", 33),
                                    tuple("james", 41),
                                    tuple("dennis", 25),
                                    tuple("sophia", 32),
                                    tuple("siro", 29))
                          .size().isEqualTo(5);
    }
}
