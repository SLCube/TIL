# SpringDataJPA

순수 JPA를 이용하면 간단한 CRUD쿼리를 반 자동으로 만들어줘서 굉장히 편리했다.  

반자동이라 표현한 이유는 다음과 같다.

```java
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    ... 이하 생략
}
```

이렇게 개발자가 코드로 직접 작성해줘야 쿼리가 생성되므로 완전 자동은 아닌셈이다. 그리고 이러한 반복은 계속될것이다. 

이런 문제를 해결해주는것이 SpringDataJPA이다.(이것만 해결해주는건 아니다)

---

순수 JPA Repository를 SpringDataJPA를 이용한 Repository로 바꾸면 이렇게 된다.

```java
public interface MemberRepository extends JpaRepository<Member, Long>{

}
```

이렇게 인터페이스를 선언만하면 save, findById, delete, findAll등등.. 대부분의 공통 메소드를 제공해준다.

---

추상화할수없는 쿼리는 어떻게 해결할까?

```java
select m from Member m where m.username = :username
```

이런 JPQL은 특정 컬럼을 대상을 조건으로 걸어 조회를 하기 때문에 공통메소드로 제공하기 어렵다. 하지만 SpringDataJPA는 인터페이스의 메소드이름을 감지해서 쿼리를 자동으로 생성해주는 전략을 제공해준다.

위와같은 코드는
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsername(String username);
}
```

이렇게!! 메소드만 정의해주면 알아서 쿼리를 만들어준다!!

이 방식의 장점은
- 메소드의 이름으로 간단하게 원하는 쿼리를 정의할수있다.
- Entity의 필드명이 바뀌면 인터페이스에 정의한 메소드 이름도 바꿔야되는데 바꾸지않으면 어플리케이션 실행시점에서 오류를 발생시켜준다.

단점은
- 조건이 많아지면 메소드이름이 길어져서 가독성이 떨어진다.
