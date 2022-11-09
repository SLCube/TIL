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