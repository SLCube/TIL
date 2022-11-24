# 메소드

## item49 매개변수가 유효한지 검사하라

메소드와 생성자 대부분은 입력되는 매개변수 값이 특정조건을 만족하길 바란다. 이런 제약은 반드시 문서화를 해야하며 메소드 몸체가 시작되기전에 유효성 검사를 해야된다. 

메소드 몸체가 시작된 후에 유효성검사를 한다면 다음과 같은 문제가 발생한다.
1.  메소드 수행도중 모호한 예외를 던질 수 있다.
2. 혹은 메소드는 잘 수행됐지만 잘못된 값을 반환할 수 있다.
3. 제일 나쁜 케이스는 어떤 객체를 이상한 상태로 만들어 미래에 해당 메소드와 관련없는곳에서 예외를 터뜨릴수 있다.

자바 7부터는 java.util.Objects.requireNonNull 메소드를 제공해줘서 더이상 null체크를 수동으로 안해도 된다. 게다가 입력값을 그대로 반환해주니 사용하는 시점에서 바로 사용할 수 있다.

이러한 원칙에도 예외가 있는데 유효성 검사비용이 너무 높거나, 암묵적 유효성검사가 수행될 때이다.

예를들어 Collections.sort(List)처럼 리스트를 정렬하는 메소드가 있다하자. 리스트안의 모든 객체들은 서로 상호비교 될수 있어야하며, 정렬과정에서 비교가 이루어진다. 만약 타입이 다른 객체가 들어있다면 ClassCastException을 내뱉을것이다.

## item50 적시에 방어적 복사본을 만들라.
이번장에서 중요한 키워드는 가변과 문서화인것같다. 문서화가 중요한 키워드인 이유는 정리하면서 알아보자

어떤 객체든 그 객체의 허락없이 객체 내부를 수정하는일이 있어선 안된다. 직접 허락하지 않더라도 간접적으로 허락할 수 있다. 다음과 같은 상황이 그 예시이다.


```java
public class Period {
    private final Date start;
    private final Date end;

    public Period(Date start, Date end) {
        if(start.compareTo(end) > 0) {
            throw new IllegalArgumentException(start + "가 " + end + "보다 늦다.");
        }
        this.start = start;
        this.end = end;
    }

    public Date start() {
        return start;
    }

    public Date end() {
        return end;
    }
}
```

위 클래스의 필드들을 private으로 선언해 외부에 직접 노출되지 않고 setter를 열지 않아 수정할 수 없어보인다. 생성자에 start가 end보다 늦을 수 없다는 조건까지 걸어 꽤 괜찮아보인다. 하지만 문제가 생긴다.

하지만 Date는 가변이기 때문에 위 클래스는 불변이 아니다.

```java
Date start = new Date();
Date end = new Date();
Period period = new Period(start, end);
end.setYear(78);
```

자바8부터는 Instant, LocalDateTime 혹은 ZonedDateTime을 사용하면 문제가 해결된다. 새로 작성하는 코드는 Date를 안쓰면되지만 예전에 작성된 낡은 코드들은 Date를 꽤 사용하고있다. 이런 상황을 대비해 이번 아이템을 알아두자.

일단 Period 내부를 보호하기 위해선 생성자에서 받은 매개변수를 각각 방어적복사 해야한다.

```java
public Period(Date start, Date end) {
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());

    if(this.start.compareTo(this.end) > 0) {
        throw new IllegalArgumentException(this.start + "가 " + this.end + "보다 늦다.");
    }
}
```

생성자를 보면 특이한점이 있다. 매개변수 유효성검사는 메소드 몸체가 실행되기 전에 수행해야 된다고 앞에서 학습했는데 위 생성자는 변수 할당을 하고 유효성검사를 수행한다.

이유는 멀티스레드 환경에서 유효성검사를 끝내고 변수에 해당 매개변수를 할당하는 그 찰나에 변경이 일어날 수 있기 때문에 먼저 복사를 한 후 복사된 필드들을 이용해 유효성검사를 하고있다. 

그럼 해당 코드는 안전할까? 다음과 같은 상황을 보자

```java
Date start = new Date();
Date end = new Date();
Period period = new Period(start, end);
period.end().setYear(78);
```

생성자를 통해 방어적복사를 끝냈기때문에 객체 내부의 end와 클라이언트의 end는 다른 존재이다. 그러나 getter를 이용해 내부의 가변인자가 그대로 클라이언트에 노출되기 때문에 Period는 불변객체가 아니다.

getter에서 반환되는 값도 방어적 복사처리를 해주자
```java
public Date start() {
    return new Date(start.getTime());
}

public Date end() {
    return new Date(end.getTime());
}
```

방어적복사는 성능의 저하가 따르고, 항상 쓸 수 있는건 아니다. 호출자가 객체 내부를 수정하지 않는다고 확신할 수 있다면 수행하지 않아도 된다. 이런 상황이라면 문서화를 확실히 해야된다.
