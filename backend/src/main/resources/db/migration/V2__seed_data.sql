-- =========================================================================
-- Mini Jira MVP : 시드 데이터 (개발/학습용)
--   목록 화면의 페이지네이션·필터·정렬을 눈으로 확인하려면 데이터가 좀 있어야 한다.
-- =========================================================================

insert into app_user (name, email, avatar_color) values
    ('박수민', 'sumin@example.com',   '#2563EB'),
    ('김지훈', 'jihoon@example.com',  '#059669'),
    ('이서연', 'seoyeon@example.com', '#D97706'),
    ('최민준', 'minjun@example.com',  '#DC2626');


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
insert into task (title, description, status, priority, assignee_id, created_at, updated_at)
select
    format('샘플 작업 %s', i),
    case when i % 11 = 0 then null
         else format('%s번째 작업의 상세 설명입니다.', i) end,
    (array['TODO', 'IN_PROGRESS', 'DONE'])[1 + (i % 3)],
    (array['LOW', 'MEDIUM', 'HIGH', 'MEDIUM', 'HIGH'])[1 + (i % 5)],
    case when i % 7 = 0 then null else 1 + (i % 4) end,
    now() - (i || ' hours')::interval,
    now() - (i || ' hours')::interval
from generate_series(1, 25) as g(i);


-- 댓글은 앞쪽 8건에만, 개수를 0~2개로 들쭉날쭉하게 넣는다.
--   목록의 '댓글 수' 컬럼(서브쿼리)이 제대로 도는지 확인하려면
--   댓글이 아예 없는 Task 도 섞여 있어야 한다.
insert into task_comment (task_id, author_id, content, created_at)
select t.id,
       1 + ((t.id + c.n) % 4),
       format('%s번 작업 관련 댓글 %s', t.id, c.n),
       t.created_at + (c.n || ' hours')::interval
from task t
cross join generate_series(1, (t.id % 3)) as c(n)
where t.id <= 8;
