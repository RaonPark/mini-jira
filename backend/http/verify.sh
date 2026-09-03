#!/usr/bin/env bash
#
# mini-jira 쓰기 API 회귀 검증
#
#   실행:  bash backend/http/verify.sh
#          BASE=http://localhost:8070 bash backend/http/verify.sh
#
#   전제:  docker compose up -d 로 DB 가 떠 있고 시드(V2)가 적용된 상태에서 bootRun 중일 것.
#          검증용 Task 를 만들고 끝에 지우므로 데이터는 남지 않는다.
#
#   주의:  git-bash 에서는 curl -d 에 한글을 넣지 말 것.
#          UTF-8 이 깨져서 서버가 MALFORMED_REQUEST(400) 를 낸다.
#          한글 본문이 필요하면 파일로 빼고 --data-binary @file 을 쓴다.
#
set -u
BASE=${BASE:-http://localhost:8070}
PASS=0; FAIL=0
export PYTHONIOENCODING=utf-8

# 시드(V2)의 고정 UUID. 시드가 gen_random_uuid() 를 안 쓴 이유가 이것이다.
U1=11111111-1111-1111-1111-111111111111   # 박수민
U2=22222222-2222-2222-2222-222222222222   # 김지훈
NOPE=00000000-0000-0000-0000-000000000000

PY='import sys,json
try: d=json.load(sys.stdin)
except Exception: print(""); sys.exit()
for k in sys.argv[1].split("."):
    if d is None: break
    d = d[int(k)] if k.isdigit() else d.get(k)
print("None" if d is None else d)'

jget() { python -c "$PY" "$1"; }
code() { curl -s -o /dev/null -w '%{http_code}' "$@"; }
ck() {
  if [ "$2" = "$3" ]; then
    printf '  \033[32mPASS\033[0m  %s  (%s)\n' "$1" "$2"; PASS=$((PASS+1))
  else
    printf '  \033[31mFAIL\033[0m  %s  expected=%s actual=%s\n' "$1" "$3" "$2"; FAIL=$((FAIL+1))
  fi
}

TID=""
cleanup() { if [ -n "$TID" ]; then curl -s -o /dev/null -X DELETE "$BASE/api/tasks/$TID"; fi }
trap cleanup EXIT

if [ "$(code "$BASE/api/users")" != "200" ]; then
  echo "서버가 $BASE 에 없다. bootRun 먼저." >&2
  exit 1
fi

echo "=== 1. POST /api/tasks ==="
HDR=$(curl -s -D- -o /tmp/mj -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' \
  -d "{\"title\":\"verify task\",\"description\":\"desc\",\"assigneeId\":\"$U1\"}")
TID=$(jget id < /tmp/mj)
ck "201 Created"    "$(echo "$HDR" | head -1 | awk '{print $2}')" "201"
ck "Location 헤더"   "$(echo "$HDR" | grep -i '^location:' | tr -d '\r' | awk '{print $2}')" "/api/tasks/$TID"
ck "assignee 조인"   "$(jget assignee.name < /tmp/mj)" "박수민"
ck "status 기본값"   "$(jget status < /tmp/mj)" "TODO"
ck "priority 기본값" "$(jget priority < /tmp/mj)" "MEDIUM"

echo
echo "=== 2. PATCH — Optional 3-상태 ==="
# 계약의 description?: string | null 은 필드 생략(변경 안 함) 과 명시적 null(비움) 을 구분한다.
curl -s -X PATCH "$BASE/api/tasks/$TID" -H 'Content-Type: application/json' -d '{"title":"renamed"}' > /tmp/mj
ck "title 만 보냄 -> 변경"     "$(jget title < /tmp/mj)" "renamed"
ck "  assignee 유지"          "$(jget assignee.name < /tmp/mj)" "박수민"
ck "  description 유지"       "$(jget description < /tmp/mj)" "desc"
curl -s -X PATCH "$BASE/api/tasks/$TID" -H 'Content-Type: application/json' -d '{"assigneeId":null}' > /tmp/mj
ck "assigneeId:null -> 해제"  "$(jget assignee < /tmp/mj)" "None"
curl -s -X PATCH "$BASE/api/tasks/$TID" -H 'Content-Type: application/json' -d '{"description":null}' > /tmp/mj
ck "description:null -> 비움" "$(jget description < /tmp/mj)" "None"
curl -s -X PATCH "$BASE/api/tasks/$TID" -H 'Content-Type: application/json' -d "{\"assigneeId\":\"$U2\"}" > /tmp/mj
ck "assigneeId 재지정"        "$(jget assignee.name < /tmp/mj)" "김지훈"

echo
echo "=== 3. PATCH status — flush ==="
# flush 가 빠지면 jOOQ 가 변경 전 행을 읽어 DB 는 바뀌었는데 응답만 옛 값이 된다.
curl -s -X PATCH "$BASE/api/tasks/$TID/status" -H 'Content-Type: application/json' -d '{"status":"DONE"}' > /tmp/mj
ck "응답이 변경 후 status" "$(jget status < /tmp/mj)" "DONE"
C=$(jget createdAt < /tmp/mj); U=$(jget updatedAt < /tmp/mj)
if [ "$C" != "$U" ]; then ck "updatedAt 갱신" "$U" "$U"; else ck "updatedAt 갱신" "createdAt 과 동일" "갱신됨"; fi
ck "GET 재조회도 DONE"    "$(curl -s "$BASE/api/tasks/$TID" | jget status)" "DONE"

echo
echo "=== 4. POST comment — flush ==="
# save() 만 하고 jOOQ 로 읽으면 아직 없는 행을 찾아 404 가 난다. saveAndFlush 가 필요하다.
curl -s -X POST "$BASE/api/tasks/$TID/comments" -H 'Content-Type: application/json' \
  -d "{\"authorId\":\"$U1\",\"content\":\"verify comment\"}" > /tmp/mj
CID=$(jget id < /tmp/mj)
ck "author 조인"  "$(jget author.name < /tmp/mj)" "박수민"
ck "content 반환" "$(jget content < /tmp/mj)" "verify comment"
ck "taskId 반환"  "$(jget taskId < /tmp/mj)" "$TID"
ck "201 Created"  "$(code -X POST "$BASE/api/tasks/$TID/comments" -H 'Content-Type: application/json' \
  -d "{\"authorId\":\"$U2\",\"content\":\"second\"}")" "201"

echo
echo "=== 5. DELETE comment — 소유 관계 ==="
# OTHER 는 반드시 방금 만든 Task 가 아니어야 한다.
# 기본 정렬(createdAt,desc) 로 뽑으면 방금 만든 게 최신이라 자기 자신이 나온다.
OTHER=$(curl -s "$BASE/api/tasks?sort=createdAt,asc&size=1" | jget content.0.id)
ck "다른 Task 경로 -> 404" "$(code -X DELETE "$BASE/api/tasks/$OTHER/comments/$CID")" "404"
ck "  code"               "$(curl -s -X DELETE "$BASE/api/tasks/$OTHER/comments/$CID" | jget code)" "COMMENT_NOT_FOUND"
ck "  실제로 안 지워짐"     "$(curl -s "$BASE/api/tasks/$TID/comments" | jget 0.id)" "$CID"
ck "올바른 경로 -> 204"    "$(code -X DELETE "$BASE/api/tasks/$TID/comments/$CID")" "204"
ck "  또 삭제 -> 404"      "$(code -X DELETE "$BASE/api/tasks/$TID/comments/$CID")" "404"

echo
echo "=== 6. 검증 (@Valid) ==="
# @Valid 가 빠지면 DTO 의 @NotBlank/@Size 가 전부 무시되고 DB CHECK 위반 500 이 된다.
BLANK='{"title":"   "}'
curl -s -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "$BLANK" > /tmp/mj
ck "공백 제목 -> 400"  "$(code -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "$BLANK")" "400"
ck "  code"           "$(jget code < /tmp/mj)" "VALIDATION_FAILED"
ck "  fieldErrors[0]" "$(jget fieldErrors.0.field < /tmp/mj)" "title"
LONG=$(python -c "print('a'*201)")
ck "201자 제목 -> 400" "$(code -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "{\"title\":\"$LONG\"}")" "400"
ck "공백 댓글 -> 400"  "$(code -X POST "$BASE/api/tasks/$TID/comments" -H 'Content-Type: application/json' \
  -d "{\"authorId\":\"$U1\",\"content\":\"  \"}")" "400"

echo
echo "=== 7. MALFORMED_REQUEST ==="
BADENUM='{"title":"ok","status":"todo"}'
BADJSON='{"title":'
curl -s -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "$BADENUM" > /tmp/mj
ck "소문자 enum -> 400" "$(code -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "$BADENUM")" "400"
ck "  code"            "$(jget code < /tmp/mj)" "MALFORMED_REQUEST"
ck "깨진 JSON -> 400"   "$(code -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "$BADJSON")" "400"

echo
echo "=== 8. 404 ==="
curl -s -X PATCH "$BASE/api/tasks/$NOPE" -H 'Content-Type: application/json' -d '{"title":"x"}' > /tmp/mj
ck "없는 Task PATCH"  "$(code -X PATCH "$BASE/api/tasks/$NOPE" -H 'Content-Type: application/json' -d '{"title":"x"}')" "404"
ck "  code"          "$(jget code < /tmp/mj)" "TASK_NOT_FOUND"
ck "없는 Task DELETE" "$(code -X DELETE "$BASE/api/tasks/$NOPE")" "404"
ck "없는 Task 에 댓글" "$(code -X POST "$BASE/api/tasks/$NOPE/comments" -H 'Content-Type: application/json' \
  -d "{\"authorId\":\"$U1\",\"content\":\"x\"}")" "404"

echo
echo "=== 9. FK 검증 (USER_NOT_FOUND) ==="
# UserNotFoundException 을 서비스에 연결하기 전에는 FK 위반이 catch-all 을 타고 500 으로 샌다.
curl -s -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' \
  -d "{\"title\":\"fk\",\"assigneeId\":\"$NOPE\"}" > /tmp/mj
ck "없는 assigneeId -> 404" "$(code -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' \
  -d "{\"title\":\"fk\",\"assigneeId\":\"$NOPE\"}")" "404"
ck "  code"                "$(jget code < /tmp/mj)" "USER_NOT_FOUND"
ck "없는 authorId -> 404"   "$(code -X POST "$BASE/api/tasks/$TID/comments" -H 'Content-Type: application/json' \
  -d "{\"authorId\":\"$NOPE\",\"content\":\"fk\"}")" "404"
ck "PATCH 의 assigneeId"    "$(code -X PATCH "$BASE/api/tasks/$TID" -H 'Content-Type: application/json' \
  -d "{\"assigneeId\":\"$NOPE\"}")" "404"

echo
echo "=== 10. 뒷정리 ==="
ck "DELETE -> 204"      "$(code -X DELETE "$BASE/api/tasks/$TID")" "204"
ck "  재조회 -> 404"     "$(code "$BASE/api/tasks/$TID")" "404"
ck "  댓글 cascade 삭제" "$(code "$BASE/api/tasks/$TID/comments")" "404"
TID=""

echo
printf '===  PASS %d   FAIL %d  ===\n' "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ]
