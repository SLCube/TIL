# JPQL

- JPA에서 EntityManager가 간단한 쿼리를 대신 생성해주지만(생성, 수정, 삭제, pk를 이용한 조회 정도..?) 이 세상은 저런것 만으로는 해결할 수 없는 문제가 많고 그런 복잡한 문제를 해결하기 위한 여러가지 방법이 있지만 지금은 아주 기초적이면서 SQL이랑 유사한 SQL을 알고 있다면 쉽게 습득 할 수 있는 JPQL을 공부해보자

## JPQL이란?
Java Persistence Query Language(JPQL) 영어 어렵다. 쉽게 말하자면 Entity를 대상으로 하는 쿼리다. 그리고 SQL과 다른점중 하나다.

간단한 예제부터 보자

|column|type|
|---|---|
|id|bigint(pk)|
|username|varchar(255)|
|age|int|

회원테이블에서 회원 이름으로 찾고싶다면?

```java
select m from Member m where m.username = :username
```

18세이상 회원만 찾고싶다면?
```java
select m from Member m where m.age > 18
```

이 글에서 JPQL의 문법을 설명하지 않았지만 SQL을 알고있다면 전혀 무리없이 읽을 수 있다.

예상하겠지만 SQL 문법과 매우 유사해 생각하고있는 대부분 SQL의 기능들이 정상적으로 작동한다(GROUP BY, HAVING, JOIN...)

- 주의! : JPQL에서 from절의 서브쿼리는 동작하지 않습니다.

---

## 명시적 조인? 묵시적 조인?
- 명시적조인이란 말 그대로 조인을 명시한다는 뜻이다.

|column|type|
|---|---|
|team_id|bigint(pk)|
|name|varchar(255)|

|column|type|
|---|---|
|member_id|bigint(pk)|
|team_id|bigint(fk)|
|username|varchar(255)|
|age|int|

member와 team테이블이 있을때 명시적 조인으로 select JPQL을 만들면

```java
select m from Member m join m.team t
```

위 JPQL은 이런 SQL로 나갈겉이다.
```sql
SELECT m.*, t.*
  FROM member m
  JOIN team t
    ON t.team_id = m.team_id
```

- 묵시적 조인이란 객체 참조를 통해 조인을 발생시키는것이다.
```java
select m, m.team from Member m
```

위 JPQL은 이런 SQL로 나갈겉이다.
```sql
SELECT m.*, t.*
  FROM member m
  JOIN team t
    ON t.team_id = m.team_id
```

join을 명시하지않았지만 join이 발생했다. JPQL을 모르는 사람이 봤다면 join이 일어날거란 예상을 하지 못했을 것이다.  

묵시적조인보단 명시적 조인을..!

---

## fetch join
@xToOne 연관관계는 기본이 EAGER 로딩으로 세팅 되있다.
EAGER로 세팅하게되면 특정 Entity만의 정보를 원하더라도 연관된 Entity정보들이 전부 join이 걸리면서 조회가 되는데, 이것은 성능저하의 원인이라 Lazy 로딩으로 세팅하는게 권장된다.

회원과 그 회원이 속한 팀을 조회하고자한다.

```java
String jpql = "select m from Member m join m.team t";
List<Member> members = em.createQuery(jpql, Member.class)
    .getResultList();

for(Member member : members){
    System.out.println("username = " + member.getUsername());
    System.out.println("teamName = " + member.getTeam().getName());
}


```

```sql
select m.* from member

select t.* from team t join member m on m.team_id = t.team_id -- 회원 수 만큼 반복...
```

소위 N + 1문제가 터지는데 이 문제를 해결하기 위해서 fetch join을 이용한다.

```java
select m from Member m join fetch m.team t
```

쉽게 말하면 fetch join을 할때만 원하는 Entity를 대상으로 즉시로딩을 한다 라고 생각하면 된다.

