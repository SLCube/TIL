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

## item51 메소드 시그니처를 신중히 설계하라

이번 아이템엔 개별아이템으로 두기 애매한 api 설계 요령들을 모아뒀다. 하나하나 살펴보자

1. 메소드 이름은 신중히 짓자.
2. 편의 메소드를 너무 많이 만들지 말자. 
- 메소드가 너무 많은 클래스는 익히고 사용하고 문서화하고 테스트, 유지보수하기 어렵다. 아주 자주 쓰일경우에만 별도의 약칭 메소드를 만들자. 애매하면 만들지 말자
3. 매개변수 목록은 되도록 짧게하자(되도록 4개 이하)
- 매개변수가 많으면 기억하기도 어렵다. IDE의 도움을 받으면 그나마 덜하지만 같은 타입의 매개변수가 여러번 나온다면 아주 끔찍하다. 순서를 바꾸기라도 하면 동작은 하지만 엉뚱한 결과를 나타낼것이다.

- 다음은 매개변수 수를 줄이는 세가지 방법이다.
    1. 메소드를 쪼개자
    1. 도우미 클래스를 만들자(Dto정도가 될거같다)
    1. 앞의 두가지 방법을 섞은것으로 객체생성 빌더패턴을 메소드에 적용시킨것 이라 생각하면 된다.
        - 책에는 예제코드가 없어 간단한 코드를 만들어 보려한다.
        ```java
        // 이 객체는 불변하지 않다.
        // setter는 꼭 필요할 경우가 아니라면 열지 않는것이 맞다.
        // 예제의 간단함을 위해 setter를 예시로 들었다.
        public class Sample {
            private int number;
            private String string;

            public int getNumber() {
                return number;
            }

            public String getString() {
                return string;
            }

            public Sample setNumber(int number) {
                this.number = number;
                return this;
            }

            public Sample setString(int string) {
                this.string = string;
                return this;
            }
        }
        ```

        ```java
        Sample sample = new Sample();
        sample.setNumber(1)
            .setString("string");
        ```

4. 매개변수의 타입으로 클래스보단 인터페이스가 낫다.

5. boolean보다 원소 2개짜리 enum이 더 낫다.

메소드의 이름상 boolean타입을 받는것 보다 원소 2개짜리 enum타입이 훨씬 더 낫다. 열거 타입을 사용하면 읽고 쓰기가 훨씬 쉬워진다. 추가로 나중에 추가 선택지를 추가하기 쉬워진다.

다음은 섭씨와 화씨온도를 원소로 선언한 열거타입이다.
```java
public enum TemperatureScale {
    FAHRENHEIT, CELSIUS
}
```

```java
public void sample(boolean isCelsius) {
    if(isCelsius) {
        // 섭씨가 어쩌구..
    } else {
        // 화씨가 어쩌구...
    }
}
```

```java
public void sample(TemperatureScale temperatureScale) {
    if(temperatureScale.equals(TemperatureScale.CELSIUS)) {
        // 섭씨가 어쩌구...
    } else {
        // 화씨가 어쩌구...
    }
}
```

enum타입을 사용하면 true, false인 boolean타입보다 읽기 명백하다. 게다가 위 예시기준으로 화씨, 섭씨뿐만아니라 켈빈온도를 추가하고 싶으면 enum타입은 원소를 하나 추가해주기만 하면 된다. 

## item52 다중정의는 신중히 사용하라
이 프로그램이 뭘 출력할지 한번 생각해보자
```java
public class CollectionClassifier {
    public static String classify(Set<?> set) {
        return "Set";
    }

    public static String classify(List<?> list) {
        return "List";
    }

    public static String classify(Collection<?> c) {
        return "others";
    }

    public static void main(String[] args) {
        Collection<?>[] collections = {
                new HashSet<String>(),
                new ArrayList<BigInteger>(),
                new HashMap<String, String>().values()
        };

        for (Collection<?> collection : collections) {
            System.out.println("classify(collection) = " + classify(collection));
        }
    }
}
```

Set, List, others가 출력될거같지만 세줄 다 others가 출력된다. 왜그럴까? 일단 다중정의(overloading)은 어느 메소드가 호출될지 컴파일타임에 결정된다. 아무리 런타임 환경에서 collections 원소들의 타입이 HashSet, ArrayList, HashMap일지라 해도 컴파일타임엔 결국 Collection타입이기 때문에 others가 출력되는 것이다.

이번엔 비슷한 프로그램이지만 overloading이 아닌 overriding의 예시를 한번 보자

```java
class Wine {
    String name() {
        return "Wine";
    }
}

class SparklingWine extends Wine {
    @Override
    String name() {
        return "SparklingWine";
    }
}

class Champagne extends SparklingWine {
    @Override
    String name() {
        return "Champagne";
    }
}

public class Overriding {

    public static void main(String[] args) {

        List<Wine> wineList = List.of(
                new Wine(), new SparklingWine(), new Champagne()
        );

        for (Wine wine : wineList) {
            System.out.println("wine.name() = " + wine.name());
        }
    }
}
```

위 코드는 Wine, SparklingWine, Champagne을 의도한대로 잘 출력한다. overriding은 어떤 메소드를 출력할지 런타임환경에서 결정한다. 컴파일타임에 아무리 Wine타입이라도 런타임환경에는 각각의 타입이 정해지기때문에 각 타입에 맞는 메소드가 호출된다.

이러한 이유로 다중정의한 메소드는 정적으로 재정의한 메소드는 동적으로 동작한다.

사용하기에 헷갈릴만한 코드는 작성하지 않는게 좋다. 특히 공개된 API의 경우엔 더더욱 그렇다. 안전하게 가려면 매개변수 수가 같은 다중정의는 피하자. 가변인수를 사용하는 메소드라면 다중정의를 사용하지 말자. 

다중정의의 문제를 해결하는 가장 간단한 방법은 메소드이름을 달리해주는것이다. 

## item53 가변인수는 신중히 사용하라

가변인수 메소드는 명시한 타입의 인수를 0개 이상 받을 수 있는 메소드이다. 메소드를 호출할 때 항상 같은 인수를 호출하는 경우가 아니라면 매우 유용하게 쓸 수 있다. 다음은 입력받은 정수들의 합을 반환해주는 메소드이다.

```java
static int sum(int... args) {
    int sum = 0;
    
    for(int arg : args) {
        sum += arg;
    }

    return sum;
}
```
sum()은 0을 sum(1, 2, 3)은 6을 반환해준다. 

경우에 따라선 인수를 1개이상 받아야될 경우도 있다. 다음은 잘못된 예시이다.

```java
static int min(int... args) {
    if(args.length == 0) {
        throw new IllegalArgumentException("인수가 1개 이상 필요합니다.");
    }

    int min = args[0];
    for (i = 1 ; i < args.length; i++) {
        if(args[i] < min) {
            miin = args[i];
        }
    }

    return min;
}
```

위 코드의 가장 큰 문제는 컴파일이 아닌 런타임환경에서 오류가 터진다는 점이다. 덤으로 코드가 더럽다. 다음은 훨씬 나은 방법이다.

```java
static int min(int firstArg, int... remaningArgs) {
    int min = firstArg;

    for(int arg : remaningArgs) {
        if(arg < min) {
            min = arg;
        }
    }

    return min;
}
```

첫번째 인수는 평범한 매개변수로 선언하고 두번째 매개변수는 가변인수로 선언하면 문제를 깔끔히 해결된다.

성능에 민감한 상황이라면 가변인수를 더욱 더 신중하게 써야된다. 가변인수 메소드가 호출될 때마다 배열이 할당돼 초기화를 하기 때문인데 이런 상황에선 다중정의를 이용해 해결할 수 있다.

예를들어 해당 메소드 호출의 95%가 3개이하의 인수를 사용한다 치면
```java
public void sample() {...}
public void sample(int a1) {...}
public void sample(int a1, int a2) {...}
public void sample(int a1, int a2, int a3) {...}
public void sample(int a1, int a2, int a3, int... rest) {...}
```

이렇게하면 호출중 5%만이 배열을 할당하기때문에 속도 저하를 크게 개선할 수 있다.

## item54 null이 아닌, 빈 컬렉션이나 배열을 반환하라.

컬렉션이나 배열이 비어있을때 null을 반환하면 안된다. 일단 사용하는 입장에서 null처리를 하는 코드를 추가로 작성해줘야된다. null을 반환하는 쪽에서도 null에 대해 특별히 다뤄줘야된다. 

혹자는 빈 컬렉션이나 배열을 생성할때 드는 비용때문에 성능저하가 오지 않을까 걱정하기도 한다. 이건 두가지 이유로 틀린 이야기인데, 첫째론 이 할당이 성능저하의 주범이라고 확인되지 않는다면 신경 쓸 수준의 차이가 아니고, 두번째는 빈 컬렉션과 배열은 새로 할당하지 않아도 반환할 수 있기 때문이다. 

일단 컬렉션의 경우 Collections.emptyList(), Collections.emptyMap(), Collections.emptySet()과 같은 불변인 빈 컬렉션을 반환해주는 메소드들이 있다. 불변 객체는 자유롭게 공유해도 안전하다.

길이가 0짜리인 배열도 미리 선언해두고 반환하면 된다. 길이가 0인 배열은 모두 불변이다.

## item55 옵셔널 반환은 신중히 하라
Java8이후부터 값이 반환될수도 있고 안될수도 있는 경우를 위해 Optional을 제공해준다.

Java8 이전에 위의 경우에 두가지 방법이 있었는데 예외를 던지거나 null을 반환하는 방법이었다. 두 방법 다 문제가 있는데 첫째 예외는 진짜 예외일경우 반환해야되고, 둘째 null을 반환하면 별도의 null처리 코드를 작성해야된다.

Optional을 사용하는건 어렵지 않다. 빈 옵셔널은 Optional.empty()를 사용하고, 값이 든 옵셔널은 Optional.of(value)를 사용한다. 만약 null이 들어갈 가능성이 있다면 Optional.ofNullable(value)를 사용하면된다. 주의해야 할 점은 절대 null을 반환하면 안된다는 점이다. null을 반환하는 행위 자체가 Optional을 도입한 취지를 완전히 무시하는 행위이다.

위에도 적었지만 Optional은 반환하는 값이 있을수도 없을수도 있을때 사용하면 된다. 단, 컬렉션, 배열, 스트림과같은 컨테이너 타입은 옵셔널로 감싸면 안된다.  item54에서 알아본것처럼 비어있는 컨테이너 타입을 반환하는것이 더 이득이다. 