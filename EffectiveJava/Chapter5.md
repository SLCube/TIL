# 5장 제네릭

## 용어 정리
|한글용어|영문용어|예|
|---|---|---|
|매개변수화 타입|parameterized type|`List<String>`|
|실제 타입 매개변수|actual type parameter|`String`|
|제네릭 타입|generic type|`List<E>`|
|정규 타입 매개변수|formal type parameter|`E`|
|비한정적 와일드카드 타입|unbounded wildcard type|`List<?>`|
|로 타입|raw type|`List`|
|한정적 타입 매개변수|bounded type parameter|`<E extends Number>`|
|재귀적 타입 한정|recursive type bound|`<T extends Comparable<T>`|
|한정적 와일드카드 타입|bounded wildcard type|`List<? extends Number>`|
|제네릭 메소드|generic method|`static <E> List<E>asList(E[] a)`|
|타입 토큰|type token|`String.class`|

## item26 로 타입(raw type)은 사용하지 말라
현재도 사용은 가능하지만 좋은 예시는 아니다. 제네릭을 지원하기 전에 컬렉션을 다음과 같이 사용 가능했다.

```java
// Stamp 인스턴스만 취급
private final Collection stamps = ...
```

이러한 상황만 기대했을거다.
```java
stamps.add(new Stamp());
...
```

raw type으로 선언하게 되면 다른 타입의 인스턴스도 넣을수 있고 컴파일되고 실행된다.
```java
stamps.add(new Coin());
stamps.add("12");
stamps.add(1);
```

raw type으로 컬렉션을 사용하면 데이터를 꺼낼때마다 형변환을 해줘야되는데 이 과정에서 잘못된 타입으로 형변환을 시도하면 ClassCastException이 발생한다.

오류는 이상적으로는 컴파일 단계에서 발견하는게 가장 좋다.

제네릭을 활용해 타입 안정성을 확보해주자.
```java
private final Collection<Stamp> stamps = ...
```

사실 이 아이템을 보기 전까지 raw type으로 선언해본적도 없고, raw type으로 선언된 코드를 본적도 없다. 그럼에도 자바는 왜 raw type을 만들었을까?

그것을 호환성 때문이다. 제네릭은 java 1.5버전에 추가됐고 그 이전에 작성된 코드들은 제네릭이 없는 자바세상에서 작성되었기 때문이다. 

모든 타입을 허용하는 컬렉션을 사용하고 싶으면(예시로 List를 사용하겠다.) raw type보다는 `List<Object>` 와 같이 선언하자.

List를 받는 메소드에는 `List<String>` 을 받을 수 있지만 `List<Object>`를 받는 메소드는 `List<String>`을 받을 수 없다.

```java
public void example1(List list){
    ...
}

public void example2(List<Object> list){
    ...
}
```

```java
List<String> list = new ArrayList<>();
example1(list);
example2(list); // 컴파일 오류 발생!!
```

`List<String>`은 List의 하위타입이지만 `List<Object>`의 하위타입은 아니기 때문이다.

제네릭을 사용하고싶지만 실제 타입 매개변수(actual type parameter)가 무엇인지 모른다면 raw type이 아닌 비한정적 와일드카드 타입(unbounded wild card type)을 사용하자.

```java
List<?> list = ...
```

raw type과 비한정적 와일드카드 타입의 차이점은 뭘까?

Collection<?>으로 선언한 컬렉션에는 null이외에 어떤 원소도 넣을 수 없다.
```java
List<?> list = new ArrayList<>();
list.add(null); // 가능
list.add(1);    // 불가능
```

대신 컬렉션을 parameter로 받는 메소드가 실제 타입 매개변수를 모를때 비한정적 와일드카드 타입을 쓰면 된다.

```java
public void printList(List<?> list) {
    for(Object o : list) {
        System.out.println("o = " + o);
    }
}
```

```java
List<String> stringList = new ArrayList<>();
stringList.add("1");
stringList.add("2");
stringList.add("3");

List<Integer> integerList = new ArrayList<>();
integerList.add(1);
integerList.add(2);
integerList.add(3);

printList(stringList);
printList(integerList);
```

이렇게 raw type을 쓰지말아야할 다양한 이유를 알아봤지만 몇가지 예외가 존재한다.

- class리터럴에는 raw type을 사용해야한다.(배열과 기본타입도 허용한다.) ex) List.class, int.class, String[].class

- instanceof 연산자를 사용할때는 raw type을 이용하자. 런타임에는 제네릭타입정보가 지워지므로 비한정적 와일드카드 타입 이외의 매개변수화 타입에는 적용할 수 없다.

```java
if(o instanceof List) {
    List<?> o = (List<?>) o;
}
```

## item27 비검사 경고를 제거하라
제네릭을 사용하기 시작하면 수많은 컴파일러 경고를 보게될 것이다. 비검사형변환 경고, 비검사 메소드 호출 경고, 비검사 매개변수화 가변인수 타임 경고, 비검사 변환 경고 등등.. 

아래는 간단한 예시이다.

```java
Set<String> stringSet = new HashSet();
```

이렇게 코드를 작성하면 컴파일러가 친절히 경고를 내줄것이다. Java7부터는 <>표시만으로도 해결할 수 있다. 실제 타입을 명시하지 않아도 컴파일러가 추론해준다.

```java
Set<String> stringset = new HashSet<>();
```

할 수 있는한 모든 비검사경고를 제거하자. 그러면 타입 안정성이 보장된다. 즉, ClassCastException이 발생할일이 없다. 경고를 제거할 수 없지만 타입 안전하다고 확신이 들면 @SuppressWarnings("unchecked")를 이용해 경고를 숨기자(확신이라는 단어로부터 거부감이 들긴하지만....)

@SuppressWarnings 어노테이션은 지역변수 선언부터 클래스 선언까지 달 수 있지만 가능한 좁은 범위에 적용하자. 그리고 위 어노테이션은 적용할 때에는 왜 적용했는지 주석으로 달아주자.

## item28 배열보다는 리스트를 사용하라.
배열과 제네릭타입의 차이점

첫째. 배열은 공변(covariant)이고 제네릭은 불공변이다(invariant). Sub클래스가 Super클래스의 하위 클래스라면 배열은 Sub[]이 Super[]의 하위 클래스가 되고, 제네릭타입은 `List<Sub>`이 `List<Super>`의 하위클래스가 아니다.

```java
Object[] objectArray = new Long[1];
objectArray[0] = "hello objectArray";

List<Object> objectList = new ArrayList<Long>();
objectList.add("hello objectList");
```

두 경우 모두 Long타입 저장소에 String을 넣지 못하는건 같지만 배열은 런타임때 에러가 발생하고 리스트를 사용하면 컴파일시점에 바로 알 수 있다.

두번째 차이점은 제네릭은 타입정보가 런타임에 소거된다. 타입정보를 컴파일시에만 검사하고 런타임에는 알 수 없다. 이는 제네릭이 탄생하기 이전 레거시 코드와 제네릭을 사용하는 코드를 함께 사용할 수 있게 해주는 매커니즘이다.

이런 차이때문에 배열과 제네릭은 같이 못쓴다.

```java
List<String>[] stringLists = new List<String>[1];   // 제네릭 타입의 배열이 허용된다 가정하자
List<Integer> intList = List.of(42);    // 42를 원소로 갖는 정수리스트 선언
Object[] objects = stringLists; // Object배열을 선언하고 List<String> 배열을 할당. 배열은 공변이라 문제 없음
objects[0] = intList;   // 정수리스트를 Object배열의 첫번째 원소로 저장.
String s stringLists[0].get(0); // 꺼내려보니 String이 아니라 정수형이네...?
```



## item29 이왕이면 제네릭타입으로 만들라
Object클래스 기반으로 만들어진 Stack 클래스
```java
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e){
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if(size == 0) {
            throw new EmptyStackException();
        }

        Object result = elements[--size];
        elements[size] = null;
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if(elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

지금 상태에서는 클라이언트가 스택에서 객체를 꺼낼때마다 형변환을 해줘야되는데 이는 타입 안전하지 않은 상황이다.

이제 제네릭 스택으로 바꿔보자

```java
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final DEFAULT_INITIAL_CAPACITY = 16;

    public Stack {
        // E와 같이 실체화 불가 타입으론 배열을 만들 수 없음.
        elements = E[DEFAULT_INITIAL_CAPACITY];
    }
    
    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if(size == 0) {
            throw new EmptyStackException();
        }

        E result = elements[--size];
        elements[size] = null;
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if(elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }    
}
```

E와 같이 실체화 불가 타입으로는 배열을 만들 수 없다. 이런 오류를 해결하기 위한 두가지 해결책이 있다.

첫째는 Object배열을 생성한 뒤 제네릭 배열로 형변환 하는것이다.

```java
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final DEFAULT_INITIAL_CAPACITY = 16;

    @SuppressWarnings("unchecked")
    public Stack() {
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }
}
```

@SuppressWarnings를 선언하지 않으면 unchecked cast 경고가 뜰것이다. 컴파일러는 타입 안전하지 않다라는걸 증명할 수 없지만 우리는 증명할 수 있다. 

elements는 private으로 선언되있어 클라이언트에서 직접적으로 접근할 수 없음. push메소드를 통해 stack에 저장되는 원소는 항상 E타입이다. 그래서 이 비검사 형변환은 항상 안전하다. @SuppressWarnings("unchecked") 어노테이션을 사용할때는 최소 범위에 적용하자.

두번째 방법은 elements필드의 타입을 E[]에서 Object[]로 바꾸는것이다.

```java
public class Stack<E> {
    private Object[] elements;
    private int size = 0;
    private static final DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    ...push 생략

    public E pop() {
        if(size == 0){
            throw new EmptyStackException();
        }

        E result = (E) elements[--size];    // 여기서 형변환을 하지 않으면 오류가 나타난다.
        elements[size] = null;
        return result;
    }

    ... 이하 생략
}
```

두 방법다 괜찮다. 첫번째 방법은 가독성이 좋다. 배열타입을 E[]로 선언해서 확실히 E타입 원소만 받겠다고 말한다. 배열에서 원소를 읽을때 형변환을 해줄 필요가 없다.

두번째 방법은 힙 오염을 막을 수 있다.(이 예제에는 해당되지 않는 내용이다 item32를 학습하며 해당내용을 확인해보자)

이번에 보인 예시에서 item28에서의 "배열보단 리스트를 우선시해라"랑 반대되는 모습을 보인다. 자바에서 리스트를 기본타입으로 지원하지 않아 ArrayList같은 제네릭타입도 결국엔 배열을 이용해 구현하게 된다. HashMap과 같은 제네릭 타입은 성능을 높일 목적으로 배열을 사용하기도 한다.

그렇다면 배열이 아닌 리스트로 스택을 구현하면 어떻게 될까?

```java
public class Stack<E> {

    private List<E> elements;

    public Stack() {
        elements = new ArrayList<>();
    }

    public void push(E e) {
        elements.add(e);
    }

    public E pop() {
        if (elements.size() == 0) {
            throw new EmptyStackException();
        }

        E result = elements.get(elements.size() - 1);
        elements.remove(elements.size() - 1);
        return result;
    }

    public boolean isEmpty() {
        return elements.size() == 0;
    }
}
```

## item30 이왕이면 제네릭 메소드로 만들라

raw type을 사용하는 잘못된 메소드
```java
public static Set union(Set s1, Set s2) {
    Set result = new HashSet(s1);
    result.add(s2);
    return result;
}
```

지금까지 그러하듯 컴파일은 되지만 타입 안전하지 않다는 경고가 발생한다.

이 경고를 없애기 위해 위 메소드를 제네릭 메소드로 바꾸겠다.

```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
    Set<E> result = new HashSet<>(s1);
    result.addAll(s2);
    return result;
}
```

제네릭 메소드로 바꾸면 타입 안전하고 쓰기 쉽다. 직접 형변환을 하지 않으니 어떤 오류나 경고없이 컴파일이 된다.

## item31 한정적 와일드카드를 사용해 API 유연성을 높이라
매개변수화 타입은 불공변이다. Type1의 하위타입 Type2가 있을때 `List<Type2>`은 `List<Type1>`의 하위타입이 아니라는거다.

조금 더 쉽게 말하면 `List<String>`은 `List<Object>`의 하위타입이 아니란소리인데 잘 생각해보면 불공변인게 맞다 왜냐면 `List<Object>`는 어떤 객체도 넣을 수 있는데 `List<String>`은 문자열만 넣을수 있기 때문이다. `List<String>`은 `List<Object>`가 하는일을 못하기때문에 하위타입이 아니다.

예를들어 Stack API를 확장해 pushAll과 popAll을 추가한다 생각해보자.

먼저 pushAll을 구현해보면

```java
public void pushAll(Iterable<E> src) {
    for(E e : src) {
        push(e);
    }
}
```

이제 동작시켜보자
```java
Stack<Number> numberStack = new Stack<>();
Iterable<Integer> integers = List.of(1, 2, 3);
numberStack.pushAll(integers);  // incompatible types error
```

우리는 Number타입의 하위타입인 Integer타입을 담은 Iterable을 pushAll메소드에 넣었다. 논리적으로 잘 동작해야 될 것 같지만 매개변수화 타입은 불공변이기 때문에 즉 `Iterable<Number>`은 `Iterable<Integer>`의 상위타입이 아니기 때문에 동작하지 않는다.

자바에선 이러한 상황을 대비해 한정적 와일드카드 타입이라는 특별한 매개변수화 타입을 지원한다. 

```java
public void pushAll(Iterable<? extends E> src) {
    for(E e : src) {
        push(e);
    }
}
```

마찬가지로 popAll을 구현해보자
```java
public void popAll(Collection<E> dst) {
    while(!isEmpty()) {
        dst.add(pop());
    }
}
```

이제 스택에 있는 원소들을 컬렉션으로 옮겨보자
```java
Collection<Object> list = new ArrayList<>();
numberStack.popAll(list);   // incompatible types error!!
```

마찬가지로 `Collection<Object>`는 `Collection<Number>`의 상위타입이 아니기때문에 incompatible types error가 발생한다. 이번에도 한정적 와일드카드를 통해 해결해보자

```java
public void popAll(Collection<? super E> dst) {
    while(!isEmpty()) {
        dst.add(pop());
    }
}
```

유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 한정적 와일드카드 타입을 사용하자. 그러나 타입을 정확히 지정해야되는 상황엔 한정적 와일드카드 타입을 쓰는것이 좋을게 하나도 없다.

다음 공식을 기억해두자

펙스(PECS) : producer-extends, consumer-super

매개변수화 타입 T가 생산자라면 `<? extends T>`를 사용하고, 소비자라면 `<? super T>`를 사용하자.

여기서 소비자, 생산자란?
- 생산자 : 위 Stack의 예시에서 pushAll의 매개변수 src는 Stack이 사용할 E인스턴스를 '생산'하므로 `<? extends T>`를 사용했다.
- 소비자 : popAll의 매개변수 dst는 Stack에서 만들어진 E 인스턴스를 소비하므로 `<? super T>`를 사용했다.

item30에 union메소드에도 적용해보자

```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
    Set<E> result = HashSet<>(s1);
    result.addAll(s2);
    return result;
}
```

해당 메소드를 이렇게 사용하면 에러가 발생할 것이다.
```java
Set<Integer> integerSet = Set.of(1, 2, 3);
Set<Double> doubleSet = Set.of(1.1, 2.2, 3.3);
Set<Number> numberSet = union(integerSet, doubleSet);   // error!!!
```

이제 union method에 PECS공식을 적용해서 고쳐보자
```java
public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2) {
    Set<E> result = HashSet<>(s1);
    result.addAll(s2);
    return result;
}
```

`<? extends E>`를 사용한 이유는 매개변수 s1, s2가 union 메소드가 사용할 E 인스턴스를 생산하므로 producer-extends에 따라 `<? extends E>`를 사용했다.

반환타입에는 한정적 와일드카드 타입을 사용하면 안된다. 만약 반환타입에도 한정적 와일드카드 타입을 사용하게되면 유연성이 올라가기는 커녕 클라이언트에도 와일드카드타입을 사용해야한다.

다음은 반환타입에 한정적 와일드카드 타입을 사용한뒤 Intellij IDE의 도움을 받아 고친 코드이다.

```java
Set<Integer> integerSet = Set.of(1, 2, 3);
Set<Double> doubleSet = Set.of(1.1, 2.2, 3.3);

Set<Number> union = (Set<Number>) union(integerSet, doubleSet); // unchecked cast
```

IDE의 도움을 받은 결과, 클라이언트에서 형변환을 해주었을때 컴파일은 되지만 unchecked cast 경고가 나타난다. 불필요한 위험성만 생길 뿐이므로 반환타입엔 한정적 와일드카드 타입을 사용하지 말자

## item32 제네릭과 가변인수를 함께 쓸 때는 신중하자
가변인수 : 메소드에 넘기는 인수를 클라이언트에서 조절할 수 있게 해준다.

가변인수 메소드를 호출하면 가변인수를 담기 위해 배열이 자동으로 하나 만들어진다.

<img src="./img/Effevtive Java item32 가변인수 메소드 호출.png">

내부로 감춰져야될 배열이 클라이언트에 노출되는것이 문제가 생겼다. 이제부터 이러한 상황이 왜 문제가 되는지 알아보자.

```java
static void dangerous(List<String>... stringLists) {
        List<Integer> intList = List.of(42);
        Object[] objects = stringLists;
        objects[0] = intList;   // 힙 오염 발생
        String s = stringLists[0].get(0);   // ClassCastException
    }
```

item28의 제네릭배열을 허용하지 않은 이유를 설명하는 코드와 같은 코드이다. 다만 제네릭 배열을 선언하는 대신 가변인수를 사용했다는 차이만 있다. 

제네릭과 가변인수를 함께 사용하는건 타입 안전하지 않다.

그러면 자바에서 제네릭배열을 직접 선언하는건 허용하지 않으면서 제네릭 가변인수를 받는 메소드는 허용한 이유는 뭘까?

답은 실무에서 편하기 때문이다. 실제로 자바라이브러리에도 제네릭 가변인수를 받는 메소드를 여럿 제공한다. 하지만 이러한 메소드들은 타입 안전하다 무슨차이일까? 

일단 자바7이후로 이러한 메소드들이 안전하다는걸 보장하는 @SafeVarargs 어노테이션을 제공한다. @Suppress("unchecked") 어노테이션과 마찬가지로 타입 안전하지 않다면 절대로 절대로 <b>절대로</b> 어노테이션을 달지 말자

그러면 이 메소드가 타입 안전한지 어떻게 확신할 수 있을까?
첫째 메소드가 이 배열에 아무것도 저장하지않고, 둘째 그 배열의 참조가 메소드 밖으로 노출되지 않는다면, 즉 이 배열이 순수하게 인수들을 전달하는 역할만 한다면 그 메소드는 안전하다 말할 수 있다.

다음 코드는 가변인수로 넘어온 인수들을 배열로 반환해주는 메소드이다. 
```java
static <T> T[] toArray(T... args) {
    return args;
}
```

편리한 유틸 메소드처럼 보이지만 위에 언급한 내용을 참고하면 배열의 참조값이 외부로 노출되는 문제가 있다. 이제 이런 메소드가 어떤 문제를 갖고 오는지 알아보자

```java
static <T> T[] pickTwo(T a, T b, T c) {
        switch (ThreadLocalRandom.current().nextInt(3)) {
            case 0:
                return toArray(a, b);
            case 1:
                return toArray(a, c);
            case 2:
                return toArray(b, c);
        }
        throw new AssertionError(); // 도달 불가능
    }
```

pickTwo 메소드를 사용하는 클라이언트는 어떤 경고도 보지 못한다. 그럼에도 pickTwo 메소드를 실행하면 cannot be cast to class 에러가 발생한다. 

디버깅모드로 살펴보자

<img src="./img/item32 가변인수 메소드 호출 디버그 모드.png">

toArray를 직접 호출할땐 타입에 맞는 배열을 선언하지만, 다른 메소드를 통해 호출하면 Object타입 배열을 선언할까?

책에는 pickTwo에 어떤타입의 매개변수가 들어오더라도 다 담을 수 있는 가장 구체적인 타입인 Object배열로 선언된다 라고만 적혀있다. 

개인적으로 곰곰히 생각해봤다. 자바에서 raw type을 허용하는 이유는 하위버전과의 호환때문이라 배웠다. 자바5버전 이전의 세상에선 raw type만을 허용했기 때문에 자바5버전 이후에 런타임환경에선 제네릭을 소거하는 방법을 선택했다고 했다.  (`List<E> -> List`) 그러면 컬렉션이 아닌 타입은 런타임환경에선 어떻게 바뀔까? 타입을 삭제시킬순 없으니 모든 타입을 받을 수 있는 Object타입으로 선언되는게 아닐까? 라는 생각을 해봤다.

정리하자면 가변인수를 담는 배열의 참조가 외부에 노출되지 않고 가변인수를 담는 배열에 데이터를 저장하지 않는, 즉 순수하게 인수들을 전달하는 역할만 수행한다면 제네릭 가변인수를 사용하더라도 타입 안전하다. 타입안전하다 판단되면 @SafeVarargs 어노테이션을 달아주자. 그래야 해당 메소드를 사용하는 클라이언트에서 경고가 나타나지않고 클라이언트에서 헷갈리지 않게된다.

추가로 @SafeVarargs 어노테이션은 재정의할 수 없는 메소드에만 달아야한다. 재정의된 메소드도 안전하다는 보장이 없기 때문이다. static메소드, final인스턴스 메소드, 그리고 자바9버전부턴 private인스턴스 메소드에서도 사용가능하다

### 그래서 힙오염이라는게 뭔데?

힙오염(Heap Pollution)이란 매개변수화 타입의 변수가 다른 타입의 객체를 참조할때 발생한다.

@SafeVarargs 어노테이션 사용이 유일한 정답일까? 그렇지 않다. item28에서 살펴본것처럼 모든 배열은 리스트로 바꿀수 있다.

```java
static <T> List<T> flatten(List<List<? extends T> lists) {
    List<T> result = new ArrayList<>();
    for(List<? extends T> list : lists) {
        result.addAll(list);
    }

    return result;
}
```

이 방법의 장점은 컴파일러가 해당 코드가 타입 안전한지 검증할 수 있다는 것이다. 개발자가 실수로 잘못 판단했을거다 라는 걱정을 할 필요도 없다. 단점은 클라이언트코드가 살짝 지저분해지고 속도가 조금 느려질 수 있다는 점이다.

## item33 타입 안전 이종 컨테이너를 고려하라
