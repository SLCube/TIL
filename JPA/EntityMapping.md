# EntityMapping

## @Entity
- JPA를 사용해 테이블과 매핑할 클래스는 @Entity를 붙인다 (필수 !!)

### 주의사항
1. 기본 생성자 필수(public or protected)
1. final class, inner class, enum, interface 사용 X
1. 저장할 필드에 final 사용 X (DB Table과 매핑되는 class 이기 때문에!!)  

### 속성
1. name
    - JPA에서 사용할 엔티티 이름을 지정함. 
    - default : 클래스 이름을 그대로 사용
    - 같은 클래스 이름이 없다면 가급적 기본값을 사용

---
## @Table
- Entity와 매핑할 테이블 지정

### 속성
| 속성 | 기능 | 기본값 | 
|---|---|---|
|name |매핑할 테이블 이름 |엔티티 이름을 사용 |
|catalog|DB catalog 매핑||
|schema|DB schema 매핑||
|uniqueConstraints(DDL)|DDL 생성 시에 유니크 제약 조건 생성||

--- 
## DB schema 자동 생성
- DDL을 Application 실행 시점에서 자동 생성
- DB Dialect를 활용해 DB에 맞는 적절한 DDL 생성
- <b style="color : red">이렇게 생성된 DDL은 개발 장비에만 사용하자!</b>
- 생성된 DDL은 운영서버에서는 사용하지 않거나 적절히 다듬은 후 사용
### 속성
```xml
<property name="hibernate.hbm2ddl.auto" value="create" />
```
|옵션 |설명 |
|---|---|
|create|application 실행 시 기존 테이블 삭제 후 다시 생성(DROP -> CREATE)|
|create-drop|application 실행 시점엔 create와 같으나 종료시점에 테이블을 DROP한다|
|update|변경부분만 반영한다.|
|validate|엔티티와 테이블이 정상 매핑되었는지 확인한다.|
|none|사용하지 않음|   

### 주의 사항
- <b style="color : red">운영 장비엔 절대!!! create, create-drop, update 사용 금지!!!</b>
- 개발 초기 단계는 create or update
- 테스트 서버에는 update or validate
- 스테이징과 운영 서버에는 validate or none

#### DB schema 자동 생성에 대한 개인적인 생각
- 사실 local 서버에선 어떤것을 써도 무방하다. 
- but <b>자동!</b>으로 DB Table에 영향을 가는 옵션이기 때문에 다른사람들과 협업하는 서버에선 웬만하면 none을 사용할 듯 하다 

## 매핑 어노테이션 정리

## @Column
|속성|설명|기본값|
|---|---|---|
|name|필드와 매핑할 테이블의 컬럼 이름|객체의 필드 명|
|insertable, updatable|등록, 변경 가능 여부|TRUE|
|nullable|null 허용 여부를 결정한다.<br>false로 설정하면 DDL생성 시 not null 제약 조건이 붙는다.|TRUE|
|unique|@Table의 uniqueConstraints와 같지만 한 컬럼에서 간단하게 유니크 제약조건을 걸 때 사용함||
|columnDefinition|DB Column정보를 직접 줄 수 있다.<br>ex) varchar(100) default 'EMPTY'||
|length|String 타입만 사용. 문자 길이 제약 조건|255|
percision, scale|BigDemical 타입에서 사용. percision은 소수점을 포함한 전체 자리수, scale은 소수의 자리수를 나타낸다|percision = 19, scale = 2|

## @Enumerated
- Java Enum Type을 매핑하는데 사용
- <b style="color : red"> 주의!!!!!! ORDINAL 사용 절대 금지!!!!!!!!!!!!!!!!!</b>

|속성|설명|기본값|
|---|---|---|
|value| - EnumType.ORDINAL : enum 순서를 DB에 저장<br>- EnumType.STRING : enum 이름을 DB에 저장| EnumType.ORDINAL

## @Temporal
- 날짜 타입을 매핑할 때 사용
- LocalDate, LocalDateTime을 사용 할 땐 생략 가능(최신 Hibernate 지원)

## @Lob
- DB의 CLOB, BLOB 타입과 매핑

## Transient
- 필드 매핑 X
- DB에 저장, 조회 X
---
## 기본 키 매핑 어노테이션
- @Id
- @GeneratedValue

## 기본 키 매핑 법
- 직접 할당 : @Id 만 사용
- 자동 생성(@GeneratedValue)
    - IDENTITY : DB에 위임 MySQL
    - SEQUENCE : DB의 시퀀스 오브젝트 사용, Oracle
        - @SequenceGenerator 필요
    - TAble 키 생성용 테이블 사용
        - 장점 : 모든 DB에 적용 가능
        - 단점 : 성능
    - AUTO : 방언에 따라 자동 지정, default


## IDENTITY 전략
- MySQL, PostgreSQL등 에서 사용(MySQL의 auto_increment)
- JPA는 보통 트랜잭션 시점에 insert SQL이 실행 되지만 auto_increment는 DB에 insert된 이후에 id값을 알 수 있다.
- identity 전략은 em.persist() 시점에 insert SQL이 실행되고 id값을 조회한다.
