# 4주차 편의점
## 프로젝트 개요
구매자의 할인 혜택과 재고 상황을 고려하여 최종 결제 금액을 계산하고 안내하는 결제 시스템을 구현한다.

## 프로젝트 흐름
1. 인삿말 출력
2. 편의점내 재고 출력
3. 구매 입력 요구
4. 프로모션 적용 가능 상품에 대해 수량을 적게 가져왔을때 그 수량만큼 추가 여부 확인
   - Y: 증정 받을 수 있는 상품을 추가
   - N: 증정 받을 수 있는 상품을 추가 X
5. 프로모션 재고가 부족할때, 일부를 프로모션 혜택 없이 결제하는 경우, 일부 수량에 대해 정가로 결제할지 여부 확인
   - Y: 일부 수량에 대해 정가로 결제
   - N: 정가로 결제해야 하는 수량만큼 제외한 후 결제 진행
6. 멤버십 할인 적용 여부 확인
   - Y: 적용
   - N: 미적용
7. 구매 영수증 출력
8. 추가 구매 여부 확인
    - Y: 재고 업데이트된 상품 목록을 확인 후 추가로 구매(1번부터 다시 수행)
    - 구매 종료

## 프로젝트 구조
```
```

## 기능 요구 사항
### 재고 관리
- 각 상품의 재고 수량을 고려해 결제 가능 여부 확인
- 곡객이 상품을 구매 할 때마다, 결제된 수량만큼 상품의 재고 차감으로 수량 관리
- 재고 차감으로 시스템은 최신 재고 상태를 유지, 다음 고객이 구매할 때 정확한 재고 정보 제공

### 프로모션 할인
- 오늘 날짜가 프로모션 기간 내에 포함된 경우에만 할인 적용
- N 구매 시 1개 무려 증정(Buy N Get 1 Free) 형태
- 프로모션 재고 내에서만 적용
- 프로모션 기간중 이라면, 프로모션 재고 우선 차감, 프로모션 재고가 부족할 경우 일반 재고 사용
- 프로모션 적용 상품에 대해 고객이 해당 수량보다 적게 가져온 경우, 필요한 수량을 추가로 가져오면 혜택을 받을 수 있음을 안내
- 프로모션 재고가 부족하여 일부 수량을 프로모션 혜택 없이 결제해야 하는 경우, 일부 수량에 대해 정가 결제에 대한 안내

### 멤버십 할인
- 멤버십 회원은 프로모션 미적용 금액의 30% 할인
- 프로모션 적용 후 남은 금액에 대해 멤버십 할인 적용
- 최대 한도 8,000원 할인

### 입력
- 구매 상품
    - `[상품명-수량]`의 형식의 구매 상품을 쉼표로 나누어 입력
- 프로모션
    - 프로모션 적용 가능 상품에 대해 수량을 적게 가져왔을 경우
        - 증정 갯수 표시
        - 추가 여부 확인
    - 프로모션 재고가 부족할 경우
        - 프로모션 할인이 적용 안되는 상품의 갯수 표기
        - 해당 수량에 대해 정가로 결제할지 여부 확인
- 멤버십 할인
- 추가 구매 여부 
    - Y: 재고 업데이트된 상품 목록을 확인 후 추가로 구매(1번부터 다시 수행)
    - 구매 종료

### 출력
- 재고
    - 구현에 필요한 상품 목록, 행사 목록을 파일 입출력으로 불러옴
    - `src/main/resources/products.md`과 `src/main/resources/promotions.md` 파일을 이용
    - 두 파일 모두 내용의 형식을 유지한다면 값은 수정 가능
- 영수증 발행
    - 편의점 이름
        - 상품명, 수량, 금액
    - 프로모션 증정
        - 증정된 상품에 대한 상품명, 수량
    - 결산
        - 총 구매액: 상품별 가격 * 수량
        - 행사 할인
        - 멤버십 할인
        - 낼 돈

### 예외 상황
- 예외 상황 시 에러 문구를 출력하고 그 부분부터 입력을 다시 받음
    - `Exception`이 아닌 `IllegalArgumentException`, `IllegalStateException` 등과 같은 명확한 유형으로 처리
- `[ERROR]`로 시작하는 오류 메시지와 함께 상황에 맞는 안내를 출력
    - 구매할 상품과 수량 형식이 올바르지 않은 경우: `[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요.`
    - 존재하지 않는 상품을 입력한 경우: `[ERROR] 존재하지 않는 상품입니다. 다시 입력해 주세요.`
    - 구매 수량이 재고 수량을 초과한 경우: `[ERROR] 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.`
    - 기타 잘못된 입력의 경우: `[ERROR] 잘못된 입력입니다. 다시 입력해 주세요.`

### 실행 결과 예시
```
안녕하세요. W편의점입니다.
현재 보유하고 있는 상품입니다.

- 콜라 1,000원 10개 탄산2+1
- 콜라 1,000원 10개
- 사이다 1,000원 8개 탄산2+1
- 사이다 1,000원 7개
- 오렌지주스 1,800원 9개 MD추천상품
- 오렌지주스 1,800원 재고 없음
- 탄산수 1,200원 5개 탄산2+1
- 탄산수 1,200원 재고 없음
- 물 500원 10개
- 비타민워터 1,500원 6개
- 감자칩 1,500원 5개 반짝할인
- 감자칩 1,500원 5개
- 초코바 1,200원 5개 MD추천상품
- 초코바 1,200원 5개
- 에너지바 2,000원 5개
- 정식도시락 6,400원 8개
- 컵라면 1,700원 1개 MD추천상품
- 컵라면 1,700원 10개

구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])
[콜라-3],[에너지바-5]

멤버십 할인을 받으시겠습니까? (Y/N)
Y 

==============W 편의점================
상품명		수량	금액
콜라		3 	3,000
에너지바 		5 	10,000
=============증	정===============
콜라		1
====================================
총구매액		8	13,000
행사할인			-1,000
멤버십할인			-3,000
내실돈			 9,000

감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)
Y

안녕하세요. W편의점입니다.
현재 보유하고 있는 상품입니다.

- 콜라 1,000원 7개 탄산2+1
- 콜라 1,000원 10개
- 사이다 1,000원 8개 탄산2+1
- 사이다 1,000원 7개
- 오렌지주스 1,800원 9개 MD추천상품
- 오렌지주스 1,800원 재고 없음
- 탄산수 1,200원 5개 탄산2+1
- 탄산수 1,200원 재고 없음
- 물 500원 10개
- 비타민워터 1,500원 6개
- 감자칩 1,500원 5개 반짝할인
- 감자칩 1,500원 5개
- 초코바 1,200원 5개 MD추천상품
- 초코바 1,200원 5개
- 에너지바 2,000원 재고 없음
- 정식도시락 6,400원 8개
- 컵라면 1,700원 1개 MD추천상품
- 컵라면 1,700원 10개

구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])
[콜라-10]

현재 콜라 4개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)
Y

멤버십 할인을 받으시겠습니까? (Y/N)
N

==============W 편의점================
상품명		수량	금액
콜라		10 	10,000
=============증	정===============
콜라		2
====================================
총구매액		10	10,000
행사할인			-2,000
멤버십할인			-0
내실돈			 8,000

감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)
Y

안녕하세요. W편의점입니다.
현재 보유하고 있는 상품입니다.

- 콜라 1,000원 재고 없음 탄산2+1
- 콜라 1,000원 7개
- 사이다 1,000원 8개 탄산2+1
- 사이다 1,000원 7개
- 오렌지주스 1,800원 9개 MD추천상품
- 오렌지주스 1,800원 재고 없음
- 탄산수 1,200원 5개 탄산2+1
- 탄산수 1,200원 재고 없음
- 물 500원 10개
- 비타민워터 1,500원 6개
- 감자칩 1,500원 5개 반짝할인
- 감자칩 1,500원 5개
- 초코바 1,200원 5개 MD추천상품
- 초코바 1,200원 5개
- 에너지바 2,000원 재고 없음
- 정식도시락 6,400원 8개
- 컵라면 1,700원 1개 MD추천상품
- 컵라면 1,700원 10개

구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])
[오렌지주스-1]

현재 오렌지주스은(는) 1개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)
Y

멤버십 할인을 받으시겠습니까? (Y/N)
Y

==============W 편의점================
상품명		수량	금액
오렌지주스		2 	3,600
=============증	정===============
오렌지주스		1
====================================
총구매액		2	3,600
행사할인			-1,800
멤버십할인			-0
내실돈			 1,800

감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)
N
```

## 프로그래밍 요구 사항
- 제한 사항
    - `indent depth`는 2까지 허용
    - `method`는 한가지 일만 수행
        - 10라인을 넘기지 않도록 구현
    - 프로그램 실행 시작점은 `Application`의 `main()`
    - 프로그램 종료 시 `System.exit()`나 `exitProcess()`를 호출하지 않음
    - 입출력을 담당하는 클래스를 별도로 구현
    - 현재 날짜와 시간을 가져오려면 `camp.nextstep.edu.missionutils.DateTimes`의 `now()`를 활용
    - 사용자 입력 값은 `camp.nextstep.edu.missionutils.Console`의 `readLine()`을 활용

- 적용 사항
    - 구현에 필요한 상품 목록, 행사 목록을 파일 입출력으로 불러옴
    - `src/main/resources/products.md`과 `src/main/resources/promotions.md` 파일을 이용
    - 두 파일 모두 내용의 형식을 유지한다면 값은 수정 가능

- 지향 사항
    - `else`사용은 지양
        - `if` 조건절에서 값을 `return`하는 방식으로 구현
    - 테스트는 기능 단위로 작성
        - UI로직은 제외(System.out, System.in, Scanner)

## 기능 목록
### 입력
- 구매 상품
    - `[상품명-수량]`의 형식의 구매 상품을 쉼표로 나누어 입력
- 프로모션
    - 프로모션 적용 가능 상품에 대해 수량을 적게 가져왔을 경우
        - 증정 갯수 표시
        - 추가 여부 확인
            - Y: 증정 받을 수 있는 상품을 추가
            - N: 증정 받을 수 있는 상품을 추가 X
    - 프로모션 재고가 부족할 경우
        - 프로모션 할인이 적용 안되는 상품의 갯수 표기
        - 해당 수량에 대해 정가로 결제할지 여부 확인
            - Y: 일부 수량에 대해 정가로 결제
            - N: 정가로 결제해야 하는 수량만큼 제외한 후 결제 진행
- 멤버십 할인
    - Y: 적용
    - N: 미적용
- 추가 구매 여부
    - Y: 재고 업데이트된 상품 목록을 확인 후 추가로 구매(1번부터 다시 수행)
    - 구매 종료

### 영수증 출력
- 구매 내역, 할인 요약 출력
- 영수증 항목
    - 구매 상품 내역: 구매한 상품명, 수량, 가격
    - 증정 상품 내역: 프로모션에 따라 무료로 제공된 증정 상품의 목록
    - 금액 정보
        - 총구매액: 구매한 상품의 총 수량과 총 금액
        - 행사할인: 프로모션에 의해 할인된 금액
        - 멤버십할인: 멤버십에 의해 추가로 할인된 금액
        - 내실돈: 최종 결제 금액
- 영수증의 구성 요소를 보기 좋게 정렬하여 고객이 쉽게 금액과 수량을 확인할 수 있게 한다.

## 오류 처리


## 테스트
