# 모바일프로그래밍 팀 프로젝트 (2025-2, 8조)

광운대학교 2025년 2학기 **모바일프로그래밍** 팀 프로젝트로 만든 Android 앱입니다.
식사·활동·체중을 기록하고 리포트로 확인할 수 있습니다.

## 주요 화면
| Activity | 역할 |
|---|---|
| `MainActivity` | 메인 화면 |
| `MealActivity` | 식단 기록 (`assets/food_master.csv` 음식 데이터) |
| `WeightActivity` | 체중 기록 |
| `ReportActivity` | 기록 리포트 |
| `DBHelper`, `CSVImporter` | SQLite DB 관리, CSV 마스터 데이터 불러오기 (`activity_master.csv` 등) |

## 실행 방법
1. Android Studio에서 이 폴더를 엽니다.
2. Gradle 동기화 후 에뮬레이터나 기기에서 `app`을 실행합니다.

## 문서
- [최종 보고서 (PDF)](보고서-8조-최종본.pdf)

## 기술 스택
Java · Android SDK · SQLite · Gradle (Kotlin DSL)
