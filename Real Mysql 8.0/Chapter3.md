# chapter3 사용자 및 권한

## 3.1 사용자 식별
MySQL은 다른 DBMS와 다르게 사용자의 계정뿐만아니라 접속지점(호스트명, 도메인, IP)까지가 계정이 된다. 쉽게 말하면 아이디와 IP주소까지가 계정이 된다는 뜻이다. 

만약 모든 해당 계정에 모든 접속지점을 허용하고 싶으면 접속지점 대신 %기호를 쓰면 된다. 

```SQL
'SLCube'@'192.0.0.1' -- 비밀번호 123
'SLCube'@'%'         -- 비밀번호 456
```

위와 같이 두개의 계정이 있을때 192.0.0.1 IP주소에서 SLCube라는 계정을 이용해 접속하고자 하면 좁은 범위의 계정을 이용해야한다. 즉 비밀번호 123을 이용해 접속을 해야한다는 것이다. 

## 3.2 사용자 계정 관리
### 3.2.1 시스템 계정과 일반 계정

MySQL 8.0부터 SYSTEM_USER 권한을 갖고 있냐에 따라 시스템 계정과 일반 계정을 구분된다.

- 시스템 계정 : 데이터베이스 서버 관리자를 위한 계정
- 일반 계정 : 응용프로그램이나 개발자를 위한 계정

시스템 계정은 시스템 계정과 일반 계정을 관리(생성 삭제 및 변경)을 할 수 있지만 일반 계정은 시스템 계정을 관리 할 수 없다.

시스템 계정으로만 수행할 수 있는 일
1. 계정관리(생성, 삭제 및 계정의 권한 부여 및 삭제)
2. 다른 세션(Connection) 또는 그 세션에서 실행중인 쿼리 강제 종료
3. 스토어드 프로그램 생성 시 DEFINER를 타 사용자로 설정

### 3.2.2 계정 생성
MySQL 8.0부터 계정 생성은 CREATE USER 명령으로, 권한 부여는 GRANT 명령으로 구분해서 실행해야 한다. 

계정을 생성할 때 선택할 수 있는 옵션들이다.
1. 계정의 인증방식과 비밀번호
2. 비밀번호 관련 옵션(비밀번호 유효기간, 이력 개수, 비밀번호 재사용 불가 기간)
3. 기본 역할(ROLE)
4. SSL 옵션
5. 계정 잠금 여부

일반적으로 많이 사용되는 옵션을 가진 CREATE USER 명령이다.
```SQL
CREATE USER 'user'@'%'
IDENTIFIED WITH 'mysql_native_password' BY 'password'
REQUIRE NONE
PASSWORD EXPIRE INTERVAL 30 DAY
ACCOUNT UNLOCK
PASSWORD HISTORY INTERVAL DEFAULT
PASSWORD REUSE INTERVAL DEFAULT
PASSWORD REQUIRE CURRENT DEFAULT;
```

#### 3.2.2.1 IDENTIFIED WITH
사용자의 인증 방식과 비밀번호를 설정한다. 

identified with 뒤에는 반드시 인증방식을 명시 해야하는데 MySQL 서버에서는 다양한 인증방식을 플러그인 형식으로 제공해준다. 가장 대표적인 4가지 방법이다.

1. Native Pluggable Authentication : 5.7버전까지 기본으로 사용된 방식. SHA-1알고리즘을 이용한 해시값을 저장 해두고 클라이언트가 보낸 값과 해시 값이 일치하는지 비교하는 인증 방식.
2. Caching SHA-2 Pluggable Authentication : MySQL 8.0버전 부터 기본으로 사용되는 방식(5.6버전부터 도입되었고 8.0버전에 보완되었다.) 내부적으로 salt를 사용해 수천번의 해시 계산을 수행해 결과를 만들기 때문에 동일한 키 값이라도 해시값이 달라질 수 있다. 해시값을 계산하는데 상당한 시간이 소요되기 때문에 해시 결과값을 메모리에 캐싱해서 이름에 캐싱이 들어가게 된다. 
3. PAM Pluggable Authentication
4. LDAP Pluggable Authentication 

#### 3.2.2.2 REQUIRE
MySQL 서버에 접속할 때 암호화된 SSL/TLS 채널을 사용할지 여부를 결정함. 설정하지 않으면 비암호화 채널로 연결하게 되지만 Caching SHA-2 Authentiaction을 사용하면 암호화된 채널만으로 MySQL 서버에 접속할 수 있게 된다. 

#### 3.2.2.3 PASSWORD EXPIRE
비밀번호 유효 기간을 설정하는 옵션이다. 개발자나 DB 관리자의 비밀번호는 유효기간을 설정하는것이 보안상 안전하지만 응용 프로그램용 계정에 유효기간을 설정하는건 위험하다. 다음은 설정 가능한 옵션들이다.

1. PASSWORD EXPIRE : 계정 생성과 동시에 비밀번호의 만료 처리
2. PASSWORD EXPIRE NEVER : 계정 비밀번호 만료 기간 없음.
3. PASSWORD EXPIRE DEFAULT : default_password_lifetime 시스템 변수에 저장된 기간으로 비밀번호의 유효기간을 결정함.
4. PASSWORD EXPIRE INTERVAL n DAY : 비밀번호 유효기간을 오늘부터 n일자로 설정

#### 3.2.2.4 PASSWORD HISTORY
한 번 사용했던 비밀번호를 재사용하지 못하게 설정하는 옵션이다. 설정 가능 옵션은 다음과 같다.

1. PASSWORD HISTORY DEFAULT : password_history 시스템 변수에 저장된 개수만큼 비밀번호의 이력을 저장하여, 저장된 이력에 남아있는 비밀번호는 재사용 할 수 없다.
2. PASSWORD HISTORY n : 비밀번호의 이력을 최근 n개만 저장한다. 저장 이력에 남아있는 비밀번호는 사용 불가하다.

이전에 사용했던 비밀번호는 MySQL DB의 password_history 테이블에 저장해둔다.

```SQL
SELECT * FROM mysql.password_history;
```

#### 3.2.2.5 PASSWORD REUSE INTERVAL
한 번 사용했던 비밀번호의 재사용 금지 기간을 설정하는 옵션이다.

1. PASSWORD REUSE INTERVAL DEFAULT : password_reuse_interval 변수에 저장된 기간으로 설정
2. PASSWORD REUSE INTERVAL n DAY : n일 이후에 비밀번호를 재사용할 수 있게 설정

#### 3.2.2.6 PASSWORD REQUIRE
비밀번호가 만료되어 새로운 비밀번호로 변경할 때 현재 비밀번호를 필요로 할지 말지 결정하는 옵션

1. PASSWORD REQUIRE CURRENT : 비밀번호를 재설정할 때 현재 비밀번호를 먼저 입력하도록 설정
2. PASSWORD REQUIRE OPTIONAL : 비밀번호를 변경할 때 현재 비밀번호를 입력하지 않아도 되도록 설정
3. PASSWORD REQUIRE DEFAULT : password_require_current 시스템 변수의 값으로 설정

#### 3.2.2.7 ACCOUNT LOCK / UNLOCK
계정 잠금여부를 결정하는 설정.

1. ACCOUNT LOCK : 계정을 사용하지 못하게 잠금
2. ACCOUNT UNLOCK : 잠긴 계정을 다시 사용 가능상태로 잠금 해제

## 3.3 비밀번호 관리
### 3.3.1 고수준 비밀번호
MySQL에선 앞서 살펴본 비밀번호 유효기간 및 재사용 금지 기능 뿐만아니라 비밀번호 유효성체크, 금칙어를 설정하는 기능이 있다. 이러한 기능을 이용하려면 validate_password 컴포넌트를 설치해야 한다.

```sql
install component 'file://component_validate_password';
```

다음 명령어를 이용해 컴포넌트에서 제공하는 시스템 변수를 확인할 수 있다.
```sql
show global variables like 'validate_password%';
```
|Variable_name|Value|
|---|---|
|validate_password.check_user_name|ON|
|validate_password.dictionary_file||
|validate_password.length|8|
|validate_password.mixed_case_count|2|
|validate_password.number_count|2|
|validate_password.policy|STRONG|
|validate_password.special_char_count|2|

- validate_password.length 시스템 변수에 설정된 길이 이상의 비밀번호가 사용되야 한다.
- validate_password.mixed_case_count, validate_password.number_count, validate_password.special_char_count : 각각 숫자, 대문자, 특수문자가 설정된 길이 이상의 비밀번호가 사용되야 한다.
- validate_password.policy
    - LOW : 비밀번호의 길이만 검증
    - MEDIUM : 비밀번호 길이를 검증하고, 숫자와 대소문자 특수문자의 배합을 검증함
    - STRONG : MEDIUM 레벨의 검증을 모두 수행하고 금칙어가 포함여부까지 검증한다.

### 3.3.2 이중 비밀번호(Dual Password)
응용프로그램이 데이터베이스 서버를 사용하게 되는데 서비스가 실행중인 상태에선 데이터베이스 계정의 비밀번호 변경이 불가능했다. 그래서 몇년동안 같은 비밀번호를 유지하는 경우가 많았다.

MySQL 8.0 버전부턴 계정의 비밀번호를 2개 설정할 수 있는 이중 비밀번호(dual password)기능이 추가되었다. (이중 비밀번호라는 번역 때문에 2개의 비밀번호 모두 만족해야한다고 오해 할 수 있지만, 둘중 하나만 맞으면 된다. 헷갈리지 않게 dual password라고 기억하자)

