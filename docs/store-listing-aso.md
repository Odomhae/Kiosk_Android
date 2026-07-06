# 스토어 리스팅 개편안 (ASO) — 문안 초안 + 스크린샷 구성안

- 날짜: 2026-07-07
- 적용 대상: Google Play Console → 스토어 등록정보 (한국어 ko-KR / 영어 en-US)
- 관련 설계: [사용자 성장 · 수익화 설계](superpowers/specs/2026-07-07-user-growth-monetization-design.md)

## 1. 앱 제목 (최대 30자)

| 언어 | 제안 | 글자 수 |
|---|---|---|
| 한국어 | `키오스크 주문 연습 - 햄버거 음성 주문` | 22자 (공백 포함) |
| 영어 | `Kiosk Order Practice - Burger` | 29자 |

- 핵심 검색어 "키오스크"를 제목 맨 앞에 배치.
- 대안(백업): `키오스크 연습 - 부모님 주문 연습 앱`

## 2. 짧은 설명 (최대 80자)

| 언어 | 제안 |
|---|---|
| 한국어 | `키오스크가 어려우신가요? 실제 결제 없이 햄버거 주문을 미리 연습해 보세요. 음성으로도 주문할 수 있어요.` |
| 영어 | `Practice ordering at a fast-food kiosk. Voice ordering, no real payment.` |

## 3. 전체 설명 (한국어 초안)

```
■ 키오스크 앞에서 당황한 적 있으신가요?

요즘은 햄버거 가게, 카페 어디서나 키오스크로 주문합니다.
이 앱은 실제 매장과 똑같은 순서로 주문을 미리 연습하는 앱입니다.
실제 결제는 전혀 일어나지 않으니 몇 번이든 마음 편히 연습하세요.

■ 이런 분께 추천합니다

• 키오스크 주문이 어려운 부모님, 어르신
• 부모님께 키오스크 사용법을 알려드리고 싶은 자녀분
• 주문 순서를 미리 익히고 싶은 모든 분
• 음성 주문 대화로 한국어/영어 회화를 연습하고 싶은 분

■ 주요 기능

• 실제 키오스크와 동일한 주문 순서: 메뉴 선택 → 옵션 → 수량 → 포장/매장 → 결제 → 완료
• 말로 주문하기: "불고기버거 세트 하나 주세요"라고 말하면 알아듣습니다
• 대화형 안내: 챗봇이 단계마다 음성으로 안내해 드립니다
• 한국어/영어 지원

■ 안심하세요

• 연습용 앱입니다. 실제 주문·결제는 일어나지 않습니다.
• 회원가입이 필요 없습니다.
```

### 전체 설명 (영어 초안)

```
Ever felt lost in front of a self-ordering kiosk?

This app lets you practice the full fast-food kiosk ordering flow —
menu, options, quantity, dine-in or to-go, and payment — with no real
payment involved. Practice as many times as you like.

FEATURES
• Same steps as a real kiosk: menu → options → quantity → dine-in/to-go → payment → done
• Voice ordering: just say "One bulgogi burger combo, please"
• A chat guide talks you through every step
• Korean and English supported

GOOD FOR
• Seniors and anyone new to self-ordering kiosks
• Family members teaching parents how to use kiosks
• Language learners practicing ordering conversations

This is a practice app. No real orders or payments are made. No sign-up required.
```

## 4. 스크린샷 구성안 (휴대전화, 8장)

공통 규칙:

- 각 스크린샷 상단에 **큰 글씨 캡션 띠**(배경색 + 흰 글씨)를 얹는다. 캡션은 기기 프레임 밖 텍스트로 처리 (Figma/Canva 등).
- 순서는 실제 주문 흐름 그대로 — "이 앱을 쓰면 이 순서로 연습하게 됩니다"가 한눈에 보이게.
- 한국어 리스팅은 한국어 UI 스크린샷, 영어 리스팅은 영어 UI 스크린샷 사용 (기기 언어 변경 후 캡처).

| # | 캡처할 화면 | 캡션 (한국어) | 연출 포인트 |
|---|---|---|---|
| 1 | 채팅 주문 화면 (OrderFragment) — 봇 인사 + 사용자 음성 말풍선이 오간 상태 | `말로 주문하는 키오스크 연습` | 대표컷. "불고기버거 세트 하나 주세요" 같은 사용자 말풍선이 보이게 |
| 2 | 전체 메뉴 화면 (FullMenuFragment) — 버거 탭 | `진짜 키오스크처럼 메뉴 고르기` | 탭(버거/사이드/음료/디저트)이 모두 보이게 |
| 3 | 옵션 선택 화면 (OptionFragment) — 세트/단품 | `세트·단품 옵션도 미리 연습` | 가격 차이가 보이는 메뉴로 캡처 |
| 4 | 수량 선택 화면 (CountFragment) | `수량 선택도 차근차근` | +/- 버튼이 크게 보이게 |
| 5 | 포장/매장 선택 화면 (TakeOutFragment) | `포장할까요, 먹고 갈까요?` | 두 버튼이 나란히 보이는 상태 |
| 6 | 결제 수단 선택 화면 (PaymentFragment) | `실제 결제는 없어요, 안심 연습` | 신뢰 메시지를 이 장에 배치 — 어르신 불안 해소 핵심 컷 |
| 7 | 주문 확인 화면 (OrderConfirmationFragment) | `주문 내역 확인까지 완벽하게` | 2개 이상 담긴 주문으로 캡처 (세트 구성 보이게) |
| 8 | 주문 완료 화면 (OrderCompleteFragment) | `주문 완료! 이제 매장에서도 자신 있게` | 마무리 컷, 성취감 강조 |

캡션 (영어, 같은 순서): `Practice kiosk ordering by voice` / `Pick from a real kiosk menu` / `Combo or single — practice options` / `Choose quantity step by step` / `Dine in or to go?` / `No real payment — practice safely` / `Review your order like a pro` / `Order complete!`

### 캡처 준비 메모

- 광고가 뜨지 않은 상태로 캡처 (배너 영역이 빈 상태거나 테스트 광고면 잘라내기).
- 스크린샷 규격: 최소 1080px, 세로 9:16 권장.
- 그래픽 이미지(1024×500)도 같은 컨셉으로: 좌측에 "키오스크, 미리 연습하세요" 문구 + 우측에 채팅 화면 목업.

## 5. 등록 순서 체크리스트 (Play Console)

1. 스토어 등록정보 → 한국어(ko-KR) 제목/짧은 설명/전체 설명 교체
2. 영어(en-US) 리스팅 동일하게 교체
3. 스크린샷 8장 교체 (언어별)
4. 그래픽 이미지 교체
5. 저장 후 심사 제출 — 리스팅 변경만으로는 새 APK 업로드 불필요
6. 개편 날짜 기록 → 4주 후 Play Console '스토어 실적'에서 검색 노출·설치 수 비교
