# chapter4 아키텍처

## 4.1 MySQL 엔진 아키텍처
MySQL의 서버는 크게 머리역할을 담당하는 MySQL엔진과 
손발 역할을 하는 스토리지 엔진으로 나뉘어져있다. 

### 4.1.1 MySQL의 전체 구조
#### 4.1.1.1 MySQL 엔진
- 커넥션 핸들러 : 클라이언트의 접속 및 쿼리 요청을 처리함
- SQL 파서
- 전처리기
- SQL 옵티마이저 : 쿼리의 최적화된 실행을 돕는다.

#### 4.1.1.2 스토리지 엔진
MySQL엔진은 요청된 SQL문장을 분석, 최적화하는 등 DBMS의 머리를 담당한다. 

실제 데이터를 디스크 스토리지에 저장하거나 디스크 스토리지로부터 데이터를 읽어오는 역할은 스토리지 엔진이 담당한다.

MySQL에서의 엔진은 하나지만 스토리지 엔진은 여러개를 둘 수 있다. 

#### 4.1.1.3 핸들러 API
MySQL엔진의 쿼리 실행기에서 데이터를 쓰거나 읽을때 각 스토리지에 읽기, 쓰기 요청을 보내는데 이를 핸들러 요청이라 한다. 여기서 사용되는 API를 핸들러 API라고 한다.

### 4.1.2 MySQL 스레딩 구조
MySQL서버는 프로세스 기반이 아닌 스레딩 기반으로 작동함.
스레드는 크게 포그라운드(Foreground)와 백그라운드(Background)로 나뉜다.

전체 약 40개의 스레드가 실행되며 그중 3개만이 Foreground 스레드이다. (약 40개라 표현한 이유는 책에서는 44개의 스레드가 실행되는걸 보여주지만, 내 맥북 환경에선 40개의 스레드가 실행되고 있었다.)

백그라운드 스레드의 갯수는 MySQL의 서버 설정에따라 달라질 수 있다. 

#### 4.1.2.1 Foreground Thread(Client Thread)
포그라운드 스레드는 최소한 MySQL서버에 접속된 클라이언트 수 만큼 존재한다. 사용자가 요청하는 쿼리문을 처리한다. 클라이언트가 작업을 마치고 커넥션을 종료하면, 해당 스레드는 다시 스레드 캐시로 되돌아가지만, 스레드 캐시에 일정 수 이상의 스레드가 있다면 스레드 캐시에 넣지 않고 스레드를 종료시킨다.

- MyISAM의 경우 데이터를 데이터버퍼, 캐시 뿐만아니라 디스크의 데이터나 인덱스 파일로부터 데이터를 읽어와 작업을 처리한다.
- InnoDB의 경우 MyISAM과 다르게 데이터버퍼, 캐시까지만 포그라운드 스레드에서 처리한다. 디스크의 데이터나 인덱스 파일로부터 데이터를 읽어올땐 백그라운드 스레드를 이용한다.

#### 4.1.2.2 Background Thread
위에서 언급했지만 InnoDB에서는 MyISAM에서 포그라운드 스레드로 처리되던 작업중 일부가 백그라운드 스레드에서 처리된다.

- Insert Buffer를 병합하는 스레드
- 로그를 디스크로 기록하는 스레드
- InnoDB 버퍼 풀의 데이터를 디스크에 기록하는 스레드
- 데이터를 버퍼로 읽어 오는 스레드
- 잠금이나 데드락을 모니터링하는 스레드

### 4.1.3 메모리 할당 및 사용 구조
#### 4.1.3.1 글로벌 메모리 영역
일반적으로 클라이언트 스레드의 수와 무관하게 하나의 메모리 공간에만 할당된다. 

- 테이블 캐시
- InnoDB 버퍼 풀
- InnoDB 어댑티브 해시 인덱스
- InnoDB 리두 로그 버퍼

#### 4.1.3.2 로컬 메모리 영역
세션 메모리 영역이라고도 표현함. MySQL 서버상에 존재하는 클라이언트가 쿼리문을 처리하는데 사용하는 메모리 영역임. 각 클라이언트 스레드 별로 독립적으로 할당되며 절대 공유되지 않는다. 

로컬 메모리 영역의 공간중에는 커넥션이 열려있는동안 계속 할당되있는 공간이 있고(커넥션 버퍼, 결과 버퍼), 쿼리를 실행하는 순간에만 할당했다 다시 해제하는 공간(소트 버퍼, 조인버퍼)도 있다.

- 정렬 버퍼
- 조인 버퍼
- 바이너리 로그 캐시
- 네트워크 버퍼

### 4.1.4 플러그인 스토리지 엔진 모델
MySQL의 독특한 구조중 하나가 플러그인 모델이다. 

플러그인에서 사용할 수 있는건 스토리지 엔진 뿐 아니라 검색어 파서, 사용자 인증을 위한 Native Authentication, Caching SHA-2 Authentication등도 모두 플러그인으로 구현되있다. 

MySQL에서 쿼리가 실행되는 과정을 살펴보면 대부분의 과정에선 MySQL엔진에서 처리되지만 마지막 데이터 읽기/쓰기 작업만 스토리지 엔진에서 처리된다. 

대부분의 과정이 엔진에서 처리된다하면 스토리지 엔진을 어떤걸 사용해도 별 차이가 없는게 아닌가 싶을 수 있지만 그렇지 않다. 그 내용은 책 뒷부분에 설명되있다 한다.

### 4.1.5 컴포넌트
MySQL 8.0부턴 플러그인 아키텍쳐를 대체하기 위해 컴포넌트 아키텍처가 추가됐다. 컴포넌트는 다음과 같은 플러그인 아키텍처의 단점을 보완하기 위해 추가됐다.

- 플러그인은 오직 MySQL 서버와 인터페이스 할 수 있고, 플러그인 끼리는 통신 불가
- 플러그인은 MySQL 서버의 변수나 함수를 직접 호출하기 때문에 안전하지 않음 (캡슐화 안됨)
- 플러그인은 상호 의존관계를 설정할 수 없어서 초기화가 어려움

### 4.1.6 쿼리 실행 구조
#### 4.1.6.1 쿼리 파서
쿼리 파서는 사용자의 요청으로 들어온 쿼리 문장을 MySQL이 인식할 수 있는 최소 단위인 토큰으로 분리해 트리 형태의 구조로 만들어내는 작업이다. 기본적인 문법 오류는 이 과정에서 모두 잡힌다.

#### 4.1.6.2 전처리기
파서 과정에서 만들어진 트리를 기반으로 쿼리 문장의 구조에 문제가 없는지 확인한다. 각 토큰을 테이블 이름이나, 컬럼명, 내장함수와 같은 개체를 매핑해 해당 객체의 존재 여부와 접근권한등을 확인한다.

#### 4.1.6.3 옵티마이저
사용자의 요청으로 들어온 쿼리 문장을 가장 저렴한 비용으로 가장 빠르게 처리하는 역할을 담당한다. DBMS의 두뇌를 담당한다.

#### 4.1.6.4 실행엔진
옵티마이저가 두뇌라면 실행엔진은 손발을 담당한다. 
실행엔진은 만들어진 계획대로 각 핸들러에게 요청해서 받은 결과를 다른 핸들러 요청의 입력으로 연결하는 역할을 수행한다.

#### 4.1.6.5 핸들러(스토리지 엔진)
실행엔진의 요청에따라 데이터를 디스크에 저장하고 디스크로부터 읽어오는 역할을한다. 핸들러는 결국 스토리지 엔진을 의미한다. 

## 4.2 InnoDB 스토리지 엔진 아키텍처
InnoDB는 MySQL의 스토리지 엔진 가운데 가장 많이 사용되는 스토리지 엔진이다. 거의 유일하게 레코드 기반 잠금을 제공하기 때문에 높은 동시성 처리가 가능하고 안정적이며 성능이 좋다.

### 4.2.1 프라이머리 키에 의한 클러스팅
InnoDB의 모든 테이블은 PK값의 순서대로 디스크에 저장된다. 모든 세컨더리 인덱스는 레코드의 주소 대신 PK값을 논리적인 주소로 사용되기때문에 PK를 이용한 레인지 스캔은 굉장히 빠르게 처리된다. PK는 다른 보조 인덱스에 비해 비중이 높게 설정되있다. 

MyISAM은 PK와 세컨더리 인덱스는 구조적으로 아무런 차이가 없다. 

### 4.2.2 외래키 지원
FK에 대한 지원은 InnoDB 스토리지 엔진 레벨에서 지원하는 기능으로 MyISAM이나 memory 테이블에선 사용할 수 없다. 

수동으로 데이터를 적재할때 외래키가 복잡하게 얽혀있다면 데이터 적재에 실패할 수 있다. 이런 경우엔 foreign_key_checks 시스템 변수를 꺼주면 된다.

```SQL
SET foreign_key_checks=OFF;
...
SET foreign_key_checks=ON;
```

FK 체크를 해제했다해서 부모 자식간 테이블 관계가 깨진상태로 둬도 된다는 뜻은 아니다. 부모 데이터를 삭제한 뒤 관련된 자식 데이터도 삭제해주고 foreign_key_checks를 다시 활성화 시켜주면 된다.

### 4.2.3 MVCC(Multi Version Concurrency Control)
일반적으로 레코드 레벨의 트랜잭션을 지원하는 DBMS에서 제공하는 기능이다. 다음은 책에서 설명한 예시이다.

```SQL
CREATE TABLE member(
    m_id INT NOT NULL, 
    m_name VARCHAR(20) NOT NULL, 
    m_area VARCHAR(100) NOT NULL, 
    PRIMARY KEY(m_id),
    INDEX ix_area(m_area)
);

INSERT INTO member(m_id, m_name, m_area) VALUES(12, '홍길동', '서울');
```

insert문이 실행되면 DB의 상태는 디스크와 InnoDB 버퍼풀에는 insert문의 정보가 기록되고 언두 로그는 비워져있는 상태이다.

이상태에서 update 쿼리를 실행시켜보자
```SQL
UPDATE member SET m_area = '경기' where m_id = 1;
```

update 문장이 실행되면 커밋여부와 상관없이 InnoDB 버퍼풀에는 새로운 값이 업데이트되고 언두로그에 기존 데이터가 복사된다. 디스크에는 설정에따라 기존내용이 유지된 상태일수도, 새로운 내용이 업데이트 됐을 수도 있다.

이러한 과정을 MVCC라고 표현한다. 하나의 레코드에 대해 2개의 버전이 유지되고 필요에 따라 어느 버전의 데이터가 보여지는지 상황에 따라 달라지는 구조이다.

update 쿼리를 rollback하면 언두로그의 내용을 InnoDB 버퍼풀로 다시 복구하고 언두 로그의 내용을 지운다.

commit을한다해도 언두로그의 내용을 지우지 않고 더이상 필요로 하는 트랜잭션이 없을때 삭제된다.

### 4.2.4 잠금 없는 일관된 읽기(Non-Locking Consistent Read)
InnoDB 스토리지 엔진은 MVCC 기술을 통해 잠금을 걸지 않고 읽기 작업을 수행한다. 4.2.3의 상황을 예를들면 데이터를 insert하고 update쿼리를 날린 다음에 커밋을 하지 않은 상태라 해보자. MVCC기술 덕분에 다른 사용자가 select 쿼리를 실행시켜도 과거의 데이터(언두로그 영역에 저장된 데이터)를 읽어온다. 즉 다른 트랜잭션과 관계없이 대기 없이 바로 읽기가 가능하다. 

언두로드 영역에 데이터가 많이 쌓이면 MySQL서버가 느려지거나 문제가 발생하는데 트랜잭션이 실행된다면 빠르게 롤백 혹은 커밋을 통해 트랜잭션을 완료시키는것이 좋다.

### 4.2.5 자동 데드락 감지
InnoDB 스토리지 엔진은 내부적으로 잠금이 교착 상태에 빠지지 않았는지 체크하기위해 잠금 대기 목록을 그래프형태로 관리한다. 데드락 감지 스레드가 주기적으로 검사해 교착상태에 빠진 트랜잭션을 찾아 강제종료하는데 일반적으로 트랜잭션의 언두 로그 양이 가장 적은 트랜잭션을 롤백한다. 

### 4.2.6 자동화된 장애 복구

### 4.2.7 InnoDB 버퍼풀
InnoDB 버퍼풀은 InnoDB 스토리지 엔진에서 가장 핵심적인 부분이다. 디스크의 데이터 파일이나 인덱스 정보를 메모리에 캐시해두는 공간이고, 쓰기 작업을 지연시켜서 일괄 작업으로 처리할 수 있게 해주는 버퍼 역할도 같이 한다.

#### 4.2.7.1 버퍼풀의 크기 설정
MySQL 5.7 버전부턴 InnoDB 버퍼풀의 크기를 동적으로 조절할 수 있게 개선됐다. 가능하면 InnoDB 버퍼풀의 크기를 적절히 작은 값으로 설정한뒤 상황에 따라 조금 씩 증가시키는 방법이 최적이다.(책에선 전체 메모리의 50%정도로 시작하라 적혀있다.)

#### 4.2.7.2 버퍼 풀의 구조
InnoDB 스토리지 엔진은 버퍼 풀이라는 거대한 메모리 공간을 페이지 크기 조각으로 쪼개어 데이터를 필요로 할 때 해당 데이터 페이지를 읽어서 각 조각에 저장한다. 

버퍼풀 페이지 크기 조각을 관리하기 위해 InnoDB 스토리지 엔진은 크게 LRU(Least Recently Used) 리스트와, 플러시 리스트, 프리 리스트 세가지 자료구조로 관리한다. 

- 프리 리스트 : InnoDB 버퍼 풀에서 실제 사용자 데이터로 채워지지 않은 비어있는 페이지들의 목록이다. 사용자릐 쿼리가 새롭게 디스크의 데이터 페이지를 읽어와야 하는 경우 사용한다.

- LRU 리스트 : 엄밀히 말하면 LRU와 MRU(Most Recently Used)가 합쳐진 구조이다. LRU의 목적은 디스크로 부터 한번 읽어온 페이지를 최대한 오랫동안 InnoD풀의 메모리에 유지해서 디스크 읽기를 최소화 하는것이다. 이름으로 알 수 있듯이 자주 사용되는 데이터들은 MRU영역에 계속 살아남게되고, 거의 사용되지 않는 데이터라면 점점 LRU끝으로 밀려나 InnoDB 버퍼풀에서 삭제 될 것이다.

- 플러시 리스트 : 디스크로 동기화되지 않는 데이터들을 관리하는 데이터 페이지이다. 일단 한번 변경이 가해진 데이터는 플러시 리스트에서 관리되고 특정 시점에 되면 디스크로 기록 되야 한다. 데이터가 변경되면 InnoDB는 변경 내용을 리두 로그에 기록하고 버퍼풀의 데이터 페이지에도 변경 내용을 반영한다.

#### 4.2.7.3 버퍼 풀과 리두로그
InnoDB의 버퍼풀과 리두로그는 매우 밀접한 관계를 갖고있다. 버퍼풀은 서버의 메모리가 허용하는 만큼 크게 설정하면 쿼리의 성능이 빨라지지만 이는 데이터 캐시 기능만 향상시키는 것이다. 버퍼풀의 쓰기 버퍼링 기능까지 향상시키려면 버퍼풀과 리두로그의 관계를 이해해야한다. 

일단 버퍼풀은 데이터가 변경되지 않은 클린 페이지, 변경된 데이터를 갖고 있는 더티 페이지로 나뉜다. 

InnoDB 버퍼풀의 더티 페이지는 특정 리두로그 엔트리와 관계를 갖고, 체크포인트가 발생하면 처음 채크포인트 LSN보다 작은 리두 로그 엔트리와 관련된 더티 페이지는 모두 디스크로 동기화 돼야 한다.

#### 4.2.7.4 버퍼 풀 플러시
일단 InnoDB 스토리지 엔진의 더티 페이지의 디스크 동기화와 관련된 시스템 변수들은 서비스를 운영할때 성능 문제가 발생하지 않는다면 특별히 건들이지 않는것이 좋다.

InnoDB 스토리지 엔진은 버퍼풀에서 아직 디스크로 기록되지 않은 더티 페이지들을 성능상의 악영향 없이 디스크에 동기화하기 위해 다음과 같이 2개의 플러시 기능을 백그라운드로 실행한다.
- 플러시 리스트 플러시
- LRU 리스트 플러시

##### 4.2.7.4.1 플러시 리스트 플러시
InnoDB 스토리지 엔진은 리두 로그 공간의 재활용을 위해 주기적으로 오래된 리두 로그 엔트리가 사용하는 공간을 비워야 한다. 그런데 오래된 리두 로그 공간을 비우려면 무조건 InnoDB 버퍼풀의 더티 페이지가 먼저 디스크로 기록되야한다. 이를 위해 InnoDB 스토리지는 주기적으로 플러시 리스트 플러시 함수를 호출한다. 

언제부터 얼마나 많은 더티 페이지를 한번에 디스크로 기록하느냐에 따라 사용자의 쿼리 처리가 악영향을 받지 않으면서 부드럽게 처리된다. 이를위해 InnoDB에선 다음과 같은 시스템 변수들을 제공한다.

- innodb_page_cleaners
- innodb_max_dirty_pages_pct_lwm
- innodb_max_dirty_pages_pct
- innodb_io_capacity
- innodb_io_capacity_max
- innodb_flush_neighbors
- innodb_adaptive_flushing
- innodb_adaptive)_flushing_lwm

##### 4.2.7.4.2 LRU 리스트 플러시 
InnoDB스토리지는 LRU리스트에서 사용 빈도가 낮은 데이터 페이지들을 제거해서 새로운 페이지들을 읽어올 공간을 만들어야하는데 이 과정에서 LRU리스트 플러시 함수가 사용된다. 

#### 4.2.7.5 버퍼 풀 상태 백업 및 복구
InnoDB의 버퍼풀은 쿼리 성능에 매우 밀접하게 연관돼 있다. 쿼리 요청이 매우 빈번한 서버를 셧다운했다가 다시 시작하고 서비스를 시작하면 쿼리 처리성능이 평소보다 1/10도 안되는 경우가 많다. 버퍼풀에 쿼리에 필요한 데이터들을 미리 준비해두기 때문에 디스크에서 데이터를 읽지 않아도 쿼리를 처리할 수 있기 때문이다. 

MySQL5.5 버전까진 주요 테이블과 인덱스를 풀스캔해서 데이터들을 버퍼풀에 적재해두는 워밍업 작업을 했는데 5.6이후 버전은 버퍼풀의 상태를 백업 및 복구하는 기능이 추가됐다. 

### 4.2.8 Double Write Buffer
InnoDB 스토리지 엔진의 리두로그는 공간의 낭비를 막기 위해 페이지에 변경된 내용만 기록한다. 이로 인해 더티 페이지를 디스크로 플러시할때 일부 데이터만 기록되면 그 페이지의 내용을 복구할 수 없을 수도 있다. 

InnoDB스토리지는 이러한 문제를 막기 위해 Double-Write 기법을 이용한다. 

실제 데이터 파일에 변경 내용을 기록하ㅈ기전에 더티페이지를 묶어서 시스템 테이블스페이스의 DoubleWrite 버퍼에 기록한다. 그리고 InnoDB 스토리지 엔진은 각 더티페이지를 파일의 적당한 위치에 하나씩 쓰기를 실행한다. 

예를들어 A, B, C, D, E가 변경이 이뤄졌을때 A, B는 정상적으로 기록됐지만 C를 기록하던 중 운영체제가 갑자기 종료됐다고 한다면, InnoDB 스토리지 엔진은 재시작될 때 항상 DoubleWrite 버퍼의 내용과 데이터파일들을 비교해 다른내용이 있다면 DoubleWrite 버퍼의 내용을 데이터파일로 복사한다. 

### 4.2.9 언두 로그
InnoDB 스토리지 엔진은 트랜잭션과 격리 수준을 보장하기 위해 DML로 변경되기 이전 버전의 데이터를 별도로 백업한다. 백업된 데이터를 언두 로그라 한다.

- 트랜잭션 보장 : 트랜잭션이 롤백되면 트랜잭션 도중 변경된 데이터로 복구해야 하는데, 이때 언두로그에 백업된 데이터를 이용해 복구한다.
- 격리 수준 보장 : 특정 커넥션에서 데이터를 변경하는 도중에 다른 커넥션에서 데이터를 조회하면, 트랜잭션 격리 수준에 맞게 변경중엔 레코드를 읽지않고, 언두로그에 백업된 데이터를 읽는다.

### 4.2.10 체인지 버퍼
RDBMS에서 레코드가 insert되거나 update될 때는 데이터 파일을 변경하는 작업뿐 아니라, 해당 테이블에 포함된 인덱스도 업데이트 하는 작업이 필요하다. 그런데 인덱스를 업데이트 하는 작업은 랜덤하게 디스크를 읽는 작업이 필요하므로 테이블에 인덱스가 많다면 상당히 많은 자원을 소모하게 된다. 

InnoDB는 변경해야할 인덱스 페이지가 버퍼풀에 있으면 바로 반영하지만, 디스크를 읽어야한다면 즉시 실행하지않고 임시공간(체인지 버퍼)에 저장하고 사용자에게 결과를 반환하는 형태로 성능을 향상시킨다.

중복여부를 체크해야하는 유니크 인덱스틑 체인지버퍼를 사용할 수 없다.

### 4.2.11 리두 로그 및 로그 버퍼
트랜잭션의 무결성을 보장하기 위해 꼭 필요한 4가지 요소(ACID)
- 'A' : atomic 트랜잭션은 원자성 작업이어야함.
- 'C' : consistent, 일관성을 의미
- 'I' : Isolated, 격리성을 의미
- 'D' : Durable, 한 번 저장된 데이터는 지속적으로 유지돼야 함을 의미.
- 일관성과 격리성을 쉽게 정의하긴 어렵지만, 서로 다른 두 개의 트랜잭션에서 동일 데이터를 변경 및 조회를 해도 상호 간섭이 없어야함을 의미한다.

리두로그는 MySQL서버가 하드웨어 및 소프트웨어 등 여러 문제점으로 인해 비정상적으로 종료됐을때 데이터 파일에 기록되지 못한 데이터를 잃지 않게 해주는 안전장치이다.

다음은 MySQL서버가 비정상 종료 됐을때 다음과 같은 두가지 경우의 일관되지 않은 데이터를 가질 수 있다.

1. 커밋됐지만 데이터 파일에 기록되지 않은 데이터
2. 롤백됐지만 데이터 파일에 이미 기록된 데이터

1번의 경우 리두 로그의 데이터를 디스크에 복사하기만 하면 된다. 2번의 경우 리두 로그로는 해결 할 수 없다. 변경되기전 데이터를 갖고있는 언두로그의 데이터를 디스크로 복사하면 해결된다. 이때 리두로그는 그 변경이 커밋됐는지, 롤백, 아니면 트랜잭션 실행 중간 상태였는지 확인하는 용도로 사용된다.

### 4.2.12 어댑티브 해시 인덱스
일반적으로 인덱스라하면 테이블에 사용자가 생성해준 B-Tree 인덱스를 의미한다. 꼭 B-Tree 인덱스가 아니라해도 사용자가 직접 테이블에 생성해둔 인덱스가 일반적으로 생각하는 인덱스 일것이다.

어댑티브 해시 인덱스는 사용자가 수동으로 생성하는 인덱스가 아니라, InnoDB 스토리지 엔진에서 사용자가 자주 요청하는 데이터에 대해 자동으로 생성하는 인덱스이며, innodb_adaptive_hash_index 시스템 변수를 이용해 활성화, 비활성화 할 수 있다.

B-Tree인덱스에서 특정 값을 찾는 과정은 매우 빠르게 처리된다고 많은 사람들이 생각하지만 결국 속도라는건 상대적인거다. B-Tree인덱스에서 특정 값을 찾는 복잡한 과정을 몇천개의 스레드로 실행하면 컴퓨터의 cpu는 엄청난 프로세스 스케줄링을 하게 되고 쿼리 성능은 당연히 떨어질것이다.

