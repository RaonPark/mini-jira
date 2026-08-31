-- =========================================================================
-- Mini Jira MVP : 시드 데이터 (개발/학습용)
--   목록 화면의 페이지네이션·필터·정렬을 눈으로 확인하려면 데이터가 좀 있어야 한다.
-- =========================================================================

-- 사용자의 UUID 는 고정값을 쓴다.
--   PK 에 default 가 없으므로 id 를 직접 넣어야 하고(엔티티가 UUID.randomUUID() 로 생성),
--   gen_random_uuid() 를 쓰면 매번 값이 달라져 테스트나 수동 확인에서
--   "3번 사용자" 를 지목할 방법이 없어진다.
insert into app_user (id, name, email, avatar_color) values
    ('11111111-1111-1111-1111-111111111111', '박수민', 'sumin@example.com',   '#2563EB'),
    ('22222222-2222-2222-2222-222222222222', '김지훈', 'jihoon@example.com',  '#059669'),
    ('33333333-3333-3333-3333-333333333333', '이서연', 'seoyeon@example.com', '#D97706'),
    ('44444444-4444-4444-4444-444444444444', '최민준', 'minjun@example.com',  '#DC2626');


-- Task 25건 = size 20 기준으로 2페이지. 페이징 동작을 바로 확인할 수 있다.
--
-- 각 속성을 '서로 다른 나머지 연산'으로 정한다는 점이 중요하다.
-- 예를 들어 status 와 priority 를 둘 다 i%3 으로 정하면
-- 'LOW 는 항상 TODO' 같은 상관관계가 생겨서 조합 필터를 테스트할 수 없다.
--   status      : i % 3
--   priority    : i % 5
--   담당자 미지정 : i % 7
--   설명 없음    : i % 11
--   created_at 은 1시간씩 어긋나게 → 정렬(created_at desc) 확인용
--
-- 담당자는 email 순으로 정렬한 뒤 offset 으로 고른다.
-- (UUID 를 여기 또 하드코딩하지 않기 위한 방법이고, 정렬 기준이 고정이라 결과도 결정적이다)
insert into task (id, title, description, status, priority, assignee_id, created_at, updated_at)
select
    gen_random_uuid(),
    format('샘플 작업 %s', i),
    case when i % 11 = 0 then null
         else format('%s번째 작업의 상세 설명입니다.', i) end,
    (array['TODO', 'IN_PROGRESS', 'DONE'])[1 + (i % 3)],
    (array['LOW', 'MEDIUM', 'HIGH', 'MEDIUM', 'HIGH'])[1 + (i % 5)],
    case when i % 7 = 0 then null
         else (select id from app_user order by email offset (i % 4) limit 1) end,
    now() - (i || ' hours')::interval,
    now() - (i || ' hours')::interval
from generate_series(1, 25) as g(i);


-- 댓글은 앞쪽 8건에만, 개수를 0~2개로 들쭉날쭉하게 넣는다.
--   목록의 '댓글 수' 컬럼(서브쿼리)이 제대로 도는지 확인하려면
--   댓글이 아예 없는 Task 도 섞여 있어야 한다.
--
-- id 가 uuid 라서 예전처럼 t.id % 3 으로 개수를 정할 수 없다.
-- created_at 역순으로 row_number() 를 매기면 위에서 만든 i 와 같은 순번이 된다.
insert into task_comment (id, task_id, author_id, content, created_at, updated_at)
select
    gen_random_uuid(),
    t.id,
    (select id from app_user order by email offset ((t.rn + c.n) % 4) limit 1),
    format('%s번 작업 관련 댓글 %s', t.rn, c.n),
    t.created_at + (c.n || ' hours')::interval,
    t.created_at + (c.n || ' hours')::interval
from (
    select id,
           created_at,
           row_number() over (order by created_at desc) as rn
    from task
) t
cross join generate_series(1, (t.rn % 3)::int) as c(n)
where t.rn <= 8;
