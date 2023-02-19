## boolean type과 getter, setter

```java
@Getter
@Setter
public class BoardDTO {
    private String title;
    private String content;
    private boolean isDeleted;
}
```

getter annotation을 사용하면 당연히, getTitle, getContent와 마찬가지로 getIsDeleted라는 메소드가 생성될줄 알았다.

실제로는 isDeleted라는 메소드가 생성된다.

setter annotation도 마찬가지로 setDeleted라는 메소드가 생성된다.

boolean타입 변수에 is prefix가 있으면, getter setter method의 naming이 다르게 선언된다. 

해결법은 Boolean type 즉 wrapper class를 이용하는것과 직접 getter setter메소드를 선언해주는것이다.

참고 : https://projectlombok.org/features/GetterSetter
