# 람다와 스트림

## 익명 클래스보다는 람다를 사용하라

자바 1.8 이전에는 함수 타입을 표현하려면 추상 메소드 하나만을 담은 인터페이스를 사용했다. 이런 인터페이스의 인스턴스를 함수 객체라 하여, 특정 함수나 동작을 나타내느데 썼다.

```java
// 낡은 방법이다.
Collections.sort(words, new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return Integer.compare(s1.length(), s2.length());
    }
});
```

자바1.8 이후부턴 이러한 인터페이스의 특별한 의미를 인정받게 됐고, 사람들은 이 인터페이스의 인스턴스를 람다식을 이용해 간결하게 표현할 수 있게 됐다.

```java
Collections.sort(words, (s1, s2) -> Integer.compare(s1.length(), s2.length()));
```

위 코드에서 s1, s2의 타입 그리고 반환타입에 대한 언급이 없지만, 우리대신 컴파일러가 타입 추론을 해줄 것이다. 

타입을 명시해야 코드가 더 명확해지는 경우를 제외하고는, 람다의 모든 매개변수 타입을 생략하자.

우리는 앞서 item26 제네릭의 raw type을 쓰지말라 했고, item29에선 이왕이면 제네릭을 쓰라했고, item30에선 제네릭 메소드를 쓰라했다. 람다를 쓰게된다면 이 조언은 2배는 중요해진다. 왜냐면 컴파일러가 타입을 추론할때엔 대부분 제네릭에서 타입정보를 받기 때문이다. 위 코드에서 `List<String>`타입으로 선언된 words를 List raw type으로 바꾸면 컴파일 오류가 나게 된다.

item34 에서 구현한 Operation열거 타입의 메소드들을 람다형식으로 구현할 수 있다.

```java
public enum Operation {
    PLUS("+", (x, y) -> x + y),
    MINUS("-", (x, y) -> x - y),
    TIMES("*", (x, y) -> x * y),
    DIVIDE("/", (x, y) -> x / y);

    private final String symbol;
    private final DoubleBinaryOperator op;

    Operation(String symbol, DoubleBinaryOperator op) {
        this.symbol = symbol;
        this.op = op;
    }


    @Override
    public String toString() {
        return symbol;
    }

    public double apply(double x, double y) {
        return op.applyAsDouble(x, y);
    }
}
```

apply 메소드를 항상 재정의했던걸 생각하면 코드가 굉장히 줄어들었다. 이렇게보면 상수별 클래스 몸체는 더이상 필요 없어보이지만 꼭 그렇지만은 않다. 람다는 이름이 없고 문서화도 못한다. 그래서 코드자체로 명확한 의미를 갖고 있지 않거나 코드 줄 수가 많다면 람다를 쓰지 말아야한다.