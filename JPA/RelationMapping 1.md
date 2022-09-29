# 연관관계 매핑 기초
<br>
<img src="./img/DB에 맞춰 객체설계.png" width="450px">

- DB는 외래키를 사용해 테이블간 연관관계를 맺지만 객체는 참조를 통해 연관관계를 맺는다.
- 위와 같이 객체를 설계해 코드를 작성하면 다음과 같이 된다.

```java

@Entity
class Member {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "user_name")
    private String name;

    @Column(name = "team_id")
    private Long teamId;
}
    
```

- 객체를 테이블에 맞춰 모델링 했을 때 생기는 문제점

```java
    // 저장
    Team team = new Team();
    team.setName("TeamA");
    em.persist(team);

    Member member = new Member();
    member.setName("member1")
    member.setTeamId(team.getId());
    em.persis(member);
```

```java
    // 조회
    Member member = em.find(Member.class, memberId);
    Long findTeamId = member.getTeamId();
    Team team = em.find(Team.class, findTeamId);
```

- 매번 member에서 teamId를 조회한 뒤 teamId로 Team을 조회 해야 될 것이다.

### 왜 이런 현상이 생기는 걸까?
1. 테이블은 FK로 조인을 이용해 연관된 테이블을 찾는다.
2. 객체는 참조를 사용해 연관된 객체를 찾는다.
---
<br>

## 단방향 연관관계
<br>
<img src="./img/객체 연관관례를 사용.png" width="450px">

```java
@Entity
class Member {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "user_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
```

- Member : Team = N : 1 이기 때문에 @ManyToOne을 사용한다.
- 반대로 Team : Member = 1 : N 이기 때문에 Team Class에선 @OneToMany를 사용해야겠지?

```java
    // 저장
    Team team = new Team();
    team.setName("TeamA");
    em.persist(team);

    Member member = new Member();
    member.setName("member1")
    member.setTeam(team);
    em.persis(member);

    em.flush();
    em.clear();

    Member findMember = em.find(Member.class, member.getId());
    Team findTeam = member.getTeam();
```
---

## 양방향 연관관계

- 기존 코드에선 Member에서 Team을 참조할 수 있었지만 Team에서 Member를 참조 할 수 없었다.
- 테이블에선 FK로 양쪽으로 참조 가능하지만 객체에선 그럴 수 없기 때문에 Team Class에 추가로 코드 작성이 필요하다.

```java
class Team {

    ...
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
    ...
}
```

- 추가한 members를 확인하는 코드
```java
    Member findMember = em.find(Member.class, member.getId());
    List<Member> members = findMember.getTeam().getMembers();

    for(Member member : members){
        System.out.println("member.getName() : " + member.getName());
    }
```

---
## 연관관계의 주인과 mappedBy
- mappedBy는 JPA의 난이도를 올리는 주범
- 객체와 테이블사이에 연관관계를 맺는 차이를 이해해야 됨.

### 객체와 테이블이 관계를 맺는 차이
- 객체 -> 연관관계 2개
    - 회원 -> 팀
    - 팀 -> 회원
- 테이블 연관관계 1개
    - 회원 <-> 팀

- 테이블은 외래 키 하나로 두 테이블의 연관관계를 관리한다.
- 우리가 양방향 연관관계를 설정해 줬지만 사실상 단방향 2개를 설정한 것과 같다.

- 우리는 두 방향중 하나의 방향으로 외래키를 관리 해야된다.
---
### 양방향 매핑 규칙
1. 객체의 두 관계중 하나를 연관관계의 주인으로 지정
2. 연관관계의 주인으로만 외래키를 관리(등록, 수정)
    - 연관관계 주인은 FK가 있는 곳을 주인으로 정하자(특수한 경우엔 반대쪽을 연관관계 주인으로 지정할 수 있지만 JPA를 처음 접했을땐 저렇게만 알아두자)
3. <b>주인이 아닌쪽은 읽기만 가능</b>
4. 주인이 아닌쪽에 mappedBy 속성으로 주인 지정