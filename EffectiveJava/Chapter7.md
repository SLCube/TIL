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

## item43 람다보다는 메소드 참조를 사용하라
위에서 람다의 간결함에 대해 알아봤다. 그런데 자바에선 함수객체를 람다보다 더 간결하게 해주는 방법을 제공한다. 바로 메소드 참조이다.

책에 있는 코드를 살펴보자
```java
map.merge(key, 1, (count, incr) -> count + incr);
```

```java
map.merge(key, 1, Integer::sum);
```

람다로 작성할 코드를 새로운 메소드에 담아 람다대신 그 메소드를 참조하는 형식이다.

메소드 참조를 이용하면 람다보다 간결하다는것 말고 또 어떤 장점이 있을까?

그 메소드가 하는일을 잘 나타내주는 이름을 갖을수 있고, 메소드를 구현한것이기 때문에 문서화를 할 수 있다. 람다에서 언급한 단점이 모두 해결된 셈이다.

## item44 표준 함수형 인터페이스를 사용하라

## item45 스트림은 주의해서 사용하라
스트림이란? 다량의 데이터처리 작업을 돕고자 자바8에 추가된 API이다.

스트림이 제공하는 추상개념 두가지
1. 스트림은 데이터 원소의 유한 혹은 무한 시퀀스(sequence)이다. 
2. 스트림 파이프라인은 이 원소들로부터 수행해야하는 연산단계를 표현한 개념이다.

스트림은 다재다능해서 사실상 모든 연산을 수행할 수 있다. 스트림을 잘 사용하면 프로그램이 짧고 간결해지지만, 잘못 사용하게되면 가독성이 떨어지고 유지보수가 어려워진다.

지금 이 내용을 정리하는 필자는 스트림에대한 경험이 매우 적다.
그래서 책에 있는 코드들을 적고 비교하는 형식으로 정리하고자 한다.
```java
// 스트림이 적용 안된 코드
public class Anagrams {
    public static void main(String[] args) throw IOException {
        File dictionary = new File(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        Map<String, Set<String>> groups = new HashMap<>();
        try(Scanner s = new Scanner(dictionary)) {
            while(s.hasNext()) {
                String word = s.next();
                groups.computeIfAbsent(alphabetize(word),
                (unused) -> new TreeSet<>()).add(word);
            }
        }

        for(Set<String> group : groups.values()) {
            if(group.size() >= minGroupSize) {
                System.out.println(group.size() + " : " + group);
            }
        }
    }

    private static String alphanetize(String s) {
        char[] a = s.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }
}
```

```java
// 스트림이 과하게 적용된 코드
public class Anagrams {
    public static void main(String args[]) {
        Path dictionary = Paths.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try(Stream<String> words = Files.lines(dictionary)) {
            words.collect(
                groupingBy(word -> word.chars.sorted()
                            .collect(StringBuilder::new, 
                            (sb, c) -> sb.append((char) c), 
                            StringBuilder::append).toString()))
            .values().stream()
            .filter(group -> group.size() >= minGroupSize)
            .map(group -> group.size() + " : " + group)
            .forEach(System.out::println);
        }
    }
}
```

```java
// 스트림을 적절하게 사용한 코드
public class Anagrams {
    public static void main(String args[]) {
        Path dictionary = Paths.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try(Stream<String> words = Files.lines(dictionary)) {
            words.collect(groupingBy(word -> alphavetize(word)))
            .values().stream()
            .filter(group -> group.size() >= minGroupSize)
            .forEach(group -> System.out.println(group.size() + " : " + group));
        }
    }

    private static String alphabetize(String s) {
        char[] a = s.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }
}
```

스트림을 사용할때 람다식을 사용하게될건데 람다에서는 타입 이름을 자주 생략하므로 매개변수 이름을 잘 지어야 가독성이 떨어지지 않는다.

스트림을 처음 쓰게되면 모든 반복문을 스트림으로 바꾸고 싶은 욕심이 생기겠지만 처음부터 과한 욕심을 부리진 말자. 스트림으로 바꿀 수 있더라도 유지보수측면에서 손해를 볼 수 있기 때문이다.

그러니 기존 코드를 스트림으로 리팩토링 하되, 새 코드가 더 나아보일때만 사용하자. 바꿔보고 가독성이 떨어지거나 팀원들이 스트림을 사용한 코드를 이해하지 못한다면 기존 방식이 더 나을 수 있다.

## item46 스트림에서는 부작용 없는 함수를 사용하라