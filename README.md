# 프로젝트 설명
### **Spring boot와 Java를 활용하여 Account(계좌 관리) 시스템을 만드는 클론 코딩 및 추가 기능 구현 프로젝트 과제**

<br>
<br>

## 기능 설명
- Account(계좌) 시스템은 사용자와 계좌의 정보를 저장하고 있으며, 외부 시스템에서 거래를 요청할 경우 거래 정보를 받아서 계좌에서 잔액을 거래금액만큼 줄이거나(결제), 거래금액만큼 늘리는(결제 취소) 거래 관리 기능을 제공하는 시스템입니다.
- 구현의 편의를 위해 사용자 생성 등의 관리는 API로 제공하지 않고 프로젝트 시작 시 자동으로 데이터가 입력되도록 하며, 계좌 추가/해지/확인, 거래 생성/거래 취소/거래 확인의 6가지 API를 제공한다.
- 거래금액을 늘리거나 줄이는 과정에서 여러 쓰레드 혹은 인스턴스에서 같은 계좌에 접근할 경우 동시성 이슈로 인한 lost update가 발생할 수 있으므로 이 부분을 해결해야만 한다.

<br>
<br>

## 명세서
### 계좌 관련 API
- 계좌 생성
  - 파라미터 : 사용자 아이디, 초기 잔액
  - 결과
    - 실패 : 사용자가 없는 경우, 계좌가 10개 초과인 경우 실패 응답
    - 성공 : 계좌번호는 10자리 랜덤 숫자 
      - 응답정보 : 사용자 아이디, 생성된 계쫘번호, 등록일시(LoacalDateTime)
  
| 사용자가 없는 경우 실패 응답 | 계좌가 10개 초과인 경우 실패 응답 | 계좌번호 생성 성공 |
|:-------------------------:|:-----------------------:|:------------------:|
|<img width="320" alt="유저정보없음" src="https://github.com/wooooozin/AccountDemo/assets/95316662/b8281b8b-95d0-4d4d-8df8-79233f8e1045">|<img width="320" alt="계좌10개초과" src="https://github.com/wooooozin/AccountDemo/assets/95316662/e1cd9b5e-e36a-4e9d-b208-2c4b49835d6b">|<img width="320" alt="계좌 생성 성공" src="https://github.com/wooooozin/AccountDemo/assets/95316662/9a89071c-6ea9-4d0e-8cb5-32b7da61bd8d">|

<br>
<br>

|H2 DB CONSOLE|
|-|
|<img width="1116" alt="계좌 생성 DB" src="https://github.com/wooooozin/AccountDemo/assets/95316662/bb0528df-09b4-4476-b9e7-3ef559bb5a63">|


<br>
<br>

- 계좌 해지
  - 결과
    - 실패 : 사용자 없는 경우, 사용자 아이디와 계좌 소유주가 다른 경우, 계좌가 이미 해지 상태인 경우, 잔액이 있는 경우 실패 응답
    - 성공 : 
      - 응답정보 : 사용자 아이디, 계좌번호, 해지일시

| 사용자 없는 경우 | 사용자 아이디와 계좌 소유주가 다른 경우 | 계좌가 이미 해지 상태인 경우 | 잔액이 있는 경우 |
|:-------------------------:|:-----------------------:|:------------------:|:------------------:|
|<img width="320" alt="사용자 없음" src="https://github.com/wooooozin/AccountDemo/assets/95316662/0e5e54be-dff8-470d-aea9-dc53c9bf45b5">|<img width="320" alt="소유주 다름" src="https://github.com/wooooozin/AccountDemo/assets/95316662/00390e47-8ff2-4f07-8746-f00ffc6aa9a8">|<img width="320" alt="이미 해지" src="https://github.com/wooooozin/AccountDemo/assets/95316662/762673c2-871a-4838-819b-7a132e81e151">| <img width="320" alt="잔액 있음" src="https://github.com/wooooozin/AccountDemo/assets/95316662/f7313b89-fe8a-47ab-8180-2c0430bd98b4">|

|계좌 해지 성공|
|-|
|<img width="320" alt="삭제성공" src="https://github.com/wooooozin/AccountDemo/assets/95316662/2ec9f0e1-500a-4cb8-b1d7-d4f4a6c2edbc">|

|H2 DB CONSOLE|
|-|
|<img width="1126" alt="H2 DB" src="https://github.com/wooooozin/AccountDemo/assets/95316662/473b32df-018b-4c83-886b-ea57edebe0aa">|


- 계좌 확인
  - 파라미터 : 사용자 아이디
  - 결과
    - 실패 : 사용자가 없는 경우 실패 응답
    - 성공 :
      - 응답정보 : 계좌번호, 잔액 정보를 Json List 형식으로 응답

| 사용자가 없는 경우 실패 응답 | 계좌번호, 잔액 정보를 Json List 형식으로 응답 |
|:-------------------------:|:-----------------------:|
|<img width="320" alt="실패" src="https://github.com/wooooozin/AccountDemo/assets/95316662/7bba3cee-7b6f-4eb3-b5aa-6d67ef93d071">|<img width="320" alt="성공" src="https://github.com/wooooozin/AccountDemo/assets/95316662/5518d97b-e28a-403f-9062-82ba81b0100c">|

|H2 DB CONSOLE|
|-|
|<img width="1144" alt="H2 DB 3" src="https://github.com/wooooozin/AccountDemo/assets/95316662/14a78fba-b695-43e4-9ddb-e084a8ac8bd5">|


<br>
<br>

### 거래(Transaction) 관련 API
- 잔액 사용
  - 파라미터 : 사용자 아이디, 계좌번호, 거래 금액
  - 결과 :
    - 실패 : 사용자 없는 경우, 사용자 아이디와 계좌 소유주가 다른 경우, 계좌가 이미 해지 상태인 경우, 거래금액이 잔액보다 큰 경우, 거래금액이 너무 작거나 큰 경우 실패 응답
    - 성공
      - 응답 정보 : 계좌번호, transaction_result, transaction_id, 거래금액, 거래일시

| 사용자가 없는 경우 | 사용자 아이디와 계좌 소유주가 다른 경우 | 계좌가 이미 해지 상태인 경우 |
|:-------------------------:|:-----------------------:|:------------------:|
|<img width="320" alt="사용자없음" src="https://github.com/wooooozin/AccountDemo/assets/95316662/46582b90-f9b5-4a86-a160-19fd04de1ee0">|<img width="320" alt="소유주다름" src="https://github.com/wooooozin/AccountDemo/assets/95316662/ecde8309-80ad-49fe-83e9-a4fd29225224">|<img width="320" alt="해지된 계좌" src="https://github.com/wooooozin/AccountDemo/assets/95316662/5fc82c96-8342-48be-946f-5edf0f9dffaf">|

| 거래금액이 잔액보다 큰 경우 | 거래금액이 너무 작거나 큰 경우 실패 응답 | 잔액 사용 성공 |
|:-------------------------:|:-----------------------:|:------------------:|
|<img width="320" alt="금액큼" src="https://github.com/wooooozin/AccountDemo/assets/95316662/5da584d6-1d65-44df-a3b2-7ead8195fb09">|<img width="320" alt="maxmin" src="https://github.com/wooooozin/AccountDemo/assets/95316662/4285d09c-dcac-4ec1-84c3-3bf80bdbba9b">|<img width="320" alt="유즈 성공" src="https://github.com/wooooozin/AccountDemo/assets/95316662/64986312-ed0f-4191-a233-5056eb02c73b">|

|H2 DB CONSOLE|
|-|
|<img width="1351" alt="H2" src="https://github.com/wooooozin/AccountDemo/assets/95316662/24f6381f-b2d3-4d17-9692-4658aae39aff">|

<br>
<br>

- 잔액 사용 취소
  - 파라미터 : transaction_id, 계좌번호, 거래금액
  - 결과
  - 실패 : 원거래 금액과 취소 금액이 다른 경우, 트랜잭션이 해당 계좌의 거래가 아닌경우 실패 응답
  - 성공 :
    - 응답정보 : 계좌번호, transaction_result, transaction_id, 취소 거래금액, 거래일시


| 원거래 금액과 취소 금액이 다른 경우 | 트랜잭션이 해당 계좌의 거래가 아닌 경우| 잔액 취소 성공 |
|:-------------------:|:------------------:|:------------------:|
|<img width="320" alt="금액 다름" src="https://github.com/wooooozin/AccountDemo/assets/95316662/f0a6e132-d65d-4823-9263-f14ff16e0edd">|<img width="320" alt="계좌 다름" src="https://github.com/wooooozin/AccountDemo/assets/95316662/6ca74500-ab62-4925-afcc-b7aee8f21767">|<img width="320" alt="성공" src="https://github.com/wooooozin/AccountDemo/assets/95316662/989495e5-b35f-4a8a-a284-5d2d0784e243">|

|H2 DB CONSOLE|
|-|
|<img width="1362" alt="H2 4" src="https://github.com/wooooozin/AccountDemo/assets/95316662/98b6c6d3-c2ca-473d-8a7e-cb0a980a7f08">|


<br>
<br>

- 거래 확인
  - 파라미터 : transaction_id
  - 결과 :
    - 실패 : 해당 transaction_id 없는 경우 실패 응답
    - 성공 :
      - 응답 정보 : 계좌번호, 거래종류(잔액 사용, 잔액 사용 취소), transaction_result, transaction_id, 거래금액, 거래일시
      - 성공거래 뿐 아니라 실패한 거래도 거래 확인할 수 있도록 합니다.  

| 해당 transaction_id 없는 경우 실패 | 성공 거래 확인 | 실패 거래 확인 |
|:-------------------:|:------------------:|:------------------:|
|<img width="320" alt="실패" src="https://github.com/wooooozin/AccountDemo/assets/95316662/c9a3e91d-6967-4245-bd81-1e0a33067048">|<img width="320" alt="성공응답" src="https://github.com/wooooozin/AccountDemo/assets/95316662/08df05c4-b432-4d2b-90fb-134e84b58591">|<img width="320" alt="실패응답" src="https://github.com/wooooozin/AccountDemo/assets/95316662/74f463d2-b861-48fb-9eec-3a0c3016ffc9">|

|H2 DB CONSOLE|
|-|
|<img width="1371" alt="H2 5" src="https://github.com/wooooozin/AccountDemo/assets/95316662/cb532145-3e05-4e15-aab4-53862baf6739">|

<br>
<br>

# 🐯 실습 프로젝트 및 과제 후기
- 계좌 생성 시 주의사항에 대한 고민
  ```TEXT
  처음에 생각했던 방법은 랜덤으로 만들어주고 -> 중복조회 해서 없으면 그 다음부터는 1++ 해서 만들었는데 
  계정에 따라 순차증가하는게 맞는것 같아서 아이디가 다르면 새로운 랜덤 계좌를 생성하고 조회 후 1++ 하고 증가한 결과가 있다면 그때 새로운 랜덤 계좌를 생성하도록 변경했다.

  변경된 코드를 테스트 하는데 오히려 시간이 많이 걸렸는데 이게 맞는 방법인지 고민이 많이 됐다.
  그리고 랜덤으로 계좌를 생성하니 이전 테스트 코드방식으로는 검증 할 수 없어
  계좌 생성 자릿수 확인으로 대체했는데 이 외 첫자리는 0이 아닌지 아니면 랜덤한 데이터를 검증하는 방법이 따로 있는지 등도
  고민이 많이 됐지만 일단 자릿수 비교로만 해결했다.
  ```
- 과제 후기
  ```TEXT
  스프링을 처음 접하다보니 여러 개념과 다양한 어노테이션과 그리고 import에 적잖이 당황했다. 
  그래도 프로젝트를 만들 때 패키지를 분류하고 스프링 WebMVC 패턴을 조금이나마 이해할 수 있던 프로젝트였다.
  10개 이상의 프로젝트는 접해봐야 그나마 감이 생길 것 같다.
  ```
