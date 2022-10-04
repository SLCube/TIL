# 프록시

## 프록시 특징
- 실제 클래스를 상속 받아서 만듦
- 실제 클래스와 겉 모양이 같다.
- 이론상 사용하는 입장에서는 진짜 객체인진 프록시 객체인지 구분하지 않고 사용하면 됨.

## JPA에서 프록시객체는 어떻게 사용될까?
<br>
<img src="./img/JPA_Proxy예시 이미지.png">

- Member를 조회할 때 Team도 조회해야 할까?
- 회원과 팀이 같이 사용되는 로직이 많다면 같이 조회하면 되겠지만, 회원만 사용하는 로직이 많으면 회원만 조회해야 할 것이다.
- 이럴때 지연로딩(Lazy)를 사용하면 된다.

```java
@Entity
public class Member{
    @Id
    @GeneratedValue
    private Long id;

    @Column(name ="username")
    private String name;

    @ManyToOne(fetch = FetchType.Lazy)
    @JoinColumn(name = "team_id")
    private Team team;
}
```
- 위와같이 지연로딩을 설정해주면 Member를 조회할 때 team은 프록시객체로 설정되고 member만 조회하는 쿼리가 나가고 team을 조회하는 쿼리는 나가지 않는다.

```java
Member findMember=  em.find(Member.class, member.getId());
Team findTeam = findMember.getTeam();
```

- 위와같이 Member를 조회한 뒤 team을 조회할 때 team을 조회하는 쿼리가 나간다.

## 즉시로딩과 지연로딩 언제 무엇을 사용하는게 좋을까?
- 실무에선 지연로딩만 쓰자.(지연로딩으로 쳐발쳐발..)
- 공부용 예제거나 간단한 토이프로젝트(테이블이 2~3개정도되는..?)에선 상관없지만 실무에서 수십개의 테이블이 얽혀있으면 개발자가 예상하지 못하는 쿼리가 나간다.
- 즉시로딩은 JPQL에서 N + 1 문제를 일으킨다.