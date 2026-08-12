#!/bin/bash
# ============================================================
# Nexus Statistics Redis 로컬 dev seed
# 오늘 (CURDATE) 의 실시간 사전 집계 데이터
# ============================================================
# 일치 조건 (모놀리식 seed-dev.sql 의 d=0 결제와 동일):
#   매장 s 의 오늘 결제 5건:
#     i=1: 09시, amount=1000+((s*100+1500)%9001),  CARD, menu=1+((s+0+1)%15), qty=2
#     i=2: 12시, amount=1000+((s*100+3000)%9001),  CARD, menu=1+((s+0+2)%15), qty=1, +menu=1+((s+9)%15)*1
#     i=3: 15시, amount=1000+((s*100+4500)%9001),  CARD, menu=1+((s+0+3)%15), qty=2
#     i=4: 18시, amount=1000+((s*100+6000)%9001),  CASH, menu=1+((s+0+4)%15), qty=1, +menu=1+((s+11)%15)*1
#     i=5: 21시, amount=1000+((s*100+7500)%9001),  CASH, menu=1+((s+0+5)%15), qty=2
#
# 실행:
#   chmod +x seed-redis-dev.sh
#   ./seed-redis-dev.sh
#
# 환경변수 (선택):
#   REDIS_HOST (default: localhost)
#   REDIS_PORT (default: 6379)
# ============================================================

REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"
# REDIS_CLI: redis-cli 명령 (호스트 설치 시) 또는 "docker exec -i <container> redis-cli" (docker 사용 시)
# 예: REDIS_CLI="docker exec -i nexus-redis redis-cli" ./seed-redis-dev.sh
REDIS_CLI="${REDIS_CLI:-redis-cli}"
DATE=$(date +%Y-%m-%d)
TTL=604800   # 7일

# 매장명 매핑 (모놀리식 seed-dev.sql 의 ELT 와 동일, 100개)
# 인덱스 1~100 사용 (인덱스 0 은 빈 문자열)
STORE_NAMES=("" \
    "강남" "강동" "강서" "강북" "관악" "광진" "구로" "금천" "노원" "도봉" \
    "동대문" "동작" "마포" "서대문" "서초" "성동" "성북" "송파" "양천" "영등포" \
    "용산" "은평" "종로" "서울중구" "중랑" \
    "수원" "성남" "고양" "용인" "부천" "안산" "안양" "평택" "시흥" "광명" \
    "의정부" "김포" "화성" "남양주" "군포" \
    "해운대" "부산진" "동래" "사상" "부산강서" "사하" "금정" "기장" "부산남구" "부산북구" \
    "대구중구" "수성" "달서" "달성" "대구동구" \
    "인천중구" "미추홀" "연수" "남동" "부평" \
    "광주북구" "광주서구" "광주남구" \
    "대전유성" "대전서구" "대전동구" \
    "울산남구" "울산동구" "울산북구" \
    "세종" \
    "제주" "서귀포" \
    "춘천" "원주" "강릉" "속초" "평창" \
    "청주" "충주" "제천" \
    "천안" "아산" "공주" "서산" \
    "포항" "경주" "안동" "구미" \
    "창원" "김해" "진주" "양산" \
    "전주" "군산" "익산" \
    "여수" "순천" "목포" \
    "김포공항" "시청")

# 메뉴명 매핑 (모놀리식 seed-dev.sql 과 동일)
MENU_NAMES=("" "아메리카노" "카페라떼" "카푸치노" "바닐라라떼" "자몽에이드" \
            "티라미수" "치즈케이크" "마카롱" \
            "크로와상" "소금빵" "초코머핀" "베이글" \
            "샌드위치" "파니니" "샐러드")

# 메뉴 가격 (카테고리별 매출 계산용)
MENU_PRICES=(0 3000 4000 4500 4500 5500 \
             6000 6500 2500 \
             3500 3000 3500 4000 \
             7000 8000 8500)

# 메뉴 → 카테고리 매핑
menu_to_category() {
    local m=$1
    if   [ $m -le 5 ];  then echo "음료"
    elif [ $m -le 8 ];  then echo "디저트"
    elif [ $m -le 12 ]; then echo "베이커리"
    else                     echo "푸드"
    fi
}

echo "=== Redis seed 시작 ($REDIS_HOST:$REDIS_PORT, $DATE) ==="

# 기존 오늘 키 정리
$REDIS_CLI -h "$REDIS_HOST" -p "$REDIS_PORT" DEL \
    "sales:today:$DATE" \
    "sales:store:ranking:$DATE" \
    "sales:menu:ranking:$DATE" \
    "sales:hourly:$DATE" \
    "sales:category:$DATE" \
    "sales:payment:$DATE" > /dev/null

# 결정적 식으로 100매장 × 5건 누적 → redis-cli pipe
{
    pos_pay_idx=1
    for s in $(seq 1 100); do
        # 매장명 = "<idx>|더벤티 <지역>점" 형식 (모놀리식 + 통계 MSA 와 동일)
        store_name="${s}|더벤티 ${STORE_NAMES[$s]}점"

        for i in 1 2 3 4 5 6 7; do
            amount=$(( 1000 + (s * 100 + i * 1500) % 9001 ))
            hour=$(case $i in 1) echo 9;; 2) echo 11;; 3) echo 13;; 4) echo 15;; 5) echo 17;; 6) echo 19;; 7) echo 21;; esac)
            method=$([ $i -le 5 ] && echo "CARD" || echo "CASH")

            # 항목 1: menu = 1 + ((s + 0 + i) % 15), qty = 1 + (i % 2)
            menu1=$(( 1 + (s + i) % 15 ))
            qty1=$(( 1 + i % 2 ))
            menu1_member="${menu1}|${MENU_NAMES[$menu1]}"
            cat1=$(menu_to_category $menu1)
            line_amount1=$(( MENU_PRICES[$menu1] * qty1 ))

            # 1. sales:today (총 매출)
            echo "INCRBY sales:today:$DATE $amount"

            # 2. sales:store:ranking (매장 매출)
            echo "ZINCRBY sales:store:ranking:$DATE $amount \"$store_name\""

            # 3. sales:hourly (시간대 매출)
            echo "HINCRBY sales:hourly:$DATE $hour $amount"

            # 4. sales:payment (결제수단)
            echo "HINCRBY sales:payment:$DATE $method $amount"

            # 5. sales:menu:ranking (메뉴 수량)
            echo "ZINCRBY sales:menu:ranking:$DATE $qty1 \"$menu1_member\""

            # 6. sales:category (카테고리 매출 = 메뉴가격 × 수량)
            echo "HINCRBY sales:category:$DATE \"$cat1\" $line_amount1"

            # 항목 2 (i % 2 == 0): 추가 메뉴
            if [ $((i % 2)) -eq 0 ]; then
                menu2=$(( 1 + (s + i + 7) % 15 ))
                menu2_member="${menu2}|${MENU_NAMES[$menu2]}"
                cat2=$(menu_to_category $menu2)
                line_amount2=${MENU_PRICES[$menu2]}

                echo "ZINCRBY sales:menu:ranking:$DATE 1 \"$menu2_member\""
                echo "HINCRBY sales:category:$DATE \"$cat2\" $line_amount2"
            fi

            # 7. 멱등성 SETNX (PaymentEventConsumer 의 processed: 패턴)
            echo "SET processed:posPayIdx:$pos_pay_idx 1 EX $TTL"
            pos_pay_idx=$((pos_pay_idx + 1))
        done
    done

    # 6개 통계 키 TTL 7일
    echo "EXPIRE sales:today:$DATE $TTL"
    echo "EXPIRE sales:store:ranking:$DATE $TTL"
    echo "EXPIRE sales:menu:ranking:$DATE $TTL"
    echo "EXPIRE sales:hourly:$DATE $TTL"
    echo "EXPIRE sales:category:$DATE $TTL"
    echo "EXPIRE sales:payment:$DATE $TTL"

} | $REDIS_CLI -h "$REDIS_HOST" -p "$REDIS_PORT" > /dev/null

echo ""
echo "=== 검증 ==="
echo -n "오늘 총 매출 (sales:today:$DATE): "
$REDIS_CLI -h "$REDIS_HOST" -p "$REDIS_PORT" GET "sales:today:$DATE"

echo ""
echo "매장 TOP 5 (sales:store:ranking:$DATE):"
$REDIS_CLI -h "$REDIS_HOST" -p "$REDIS_PORT" ZREVRANGE "sales:store:ranking:$DATE" 0 4 WITHSCORES

echo ""
echo "메뉴 TOP 5 (sales:menu:ranking:$DATE):"
$REDIS_CLI -h "$REDIS_HOST" -p "$REDIS_PORT" ZREVRANGE "sales:menu:ranking:$DATE" 0 4 WITHSCORES

echo ""
echo "시간대별 (sales:hourly:$DATE):"
$REDIS_CLI -h "$REDIS_HOST" -p "$REDIS_PORT" HGETALL "sales:hourly:$DATE"

echo ""
echo "카테고리별 (sales:category:$DATE):"
$REDIS_CLI -h "$REDIS_HOST" -p "$REDIS_PORT" HGETALL "sales:category:$DATE"

echo ""
echo "결제 수단별 (sales:payment:$DATE):"
$REDIS_CLI -h "$REDIS_HOST" -p "$REDIS_PORT" HGETALL "sales:payment:$DATE"

echo ""
echo "=== 완료 ==="
