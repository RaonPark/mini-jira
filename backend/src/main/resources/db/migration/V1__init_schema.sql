-- =========================================================================
-- Mini Jira MVP : 초기 스키마
-- PostgreSQL 16
-- =========================================================================

-- updated_at 자동 갱신 트리거
-- PostgreSQL 에는 MySQL 의 ON UPDATE CURRENT_TIMESTAMP 가 없어 트리거로 처리한다.
create
or replace function set_updated_at() returns trigger as $$
begin
    new.updated_at
= now();
return new;
end;
$$
language plpgsql;


-- -------------------------------------------------------------------------
-- app_user : 담당자 선택 / 댓글 작성자
--   "user" 는 PostgreSQL 예약어이므로 app_user 로 명명한다.
-- -------------------------------------------------------------------------
create table app_user
(
    id           bigserial primary key,
    name         varchar(50)  not null,
    email        varchar(255) not null,
    avatar_color varchar(7)   not null default '#6B7280', -- 이니셜 아바타 배경색 (#RRGGBB)
    created_at   timestamptz  not null default now(),

    constraint uq_app_user_email unique (email)
);


-- -------------------------------------------------------------------------
-- task
--   status / priority : native enum 대신 varchar + CHECK 를 쓴다.
--     - enum 값 추가/변경 마이그레이션이 CHECK 재정의로 끝난다
--       (native enum 은 ALTER TYPE 이 까다롭고 값 삭제가 사실상 불가)
--     - jOOQ forcedType(EnumConverter) 으로 Java enum 매핑은 동일하게 얻는다
-- -------------------------------------------------------------------------
create table task
(
    id          bigserial primary key,
    title       varchar(200) not null,
    description text,
    status      varchar(20)  not null default 'TODO',
    priority    varchar(10)  not null default 'MEDIUM',
    assignee_id bigint, -- null = 담당자 미지정
    created_at  timestamptz  not null default now(),
    updated_at  timestamptz  not null default now(),

    constraint ck_task_status check (status in ('TODO', 'IN_PROGRESS', 'DONE')),
    constraint ck_task_priority check (priority in ('LOW', 'MEDIUM', 'HIGH')),
    constraint ck_task_title_not_blank check (btrim(title) <> ''),

    -- 담당자가 삭제되면 Task 는 남기고 '미지정' 으로 되돌린다
    constraint fk_task_assignee foreign key (assignee_id)
        references app_user (id) on delete set null
);

create trigger trg_task_updated_at
    before update
    on task
    for each row execute function set_updated_at();

-- 목록 기본 정렬은 created_at desc.
--   (status, created_at desc) : 상태 필터 + 정렬을 한 인덱스로 커버
--   (created_at desc)         : 필터 없는 전체 목록의 정렬 커버
create index idx_task_status_created_at on task (status, created_at desc);
create index idx_task_assignee_created_at on task (assignee_id, created_at desc);
create index idx_task_created_at on task (created_at desc);


-- -------------------------------------------------------------------------
-- task_comment
--   테이블명을 comment 로 두면 생성 코드의 Comment 클래스가
--   org.jooq.Comment 와 헷갈리므로 task_comment 로 명명한다.
-- -------------------------------------------------------------------------
create table task_comment
(
    id         bigserial primary key,
    task_id    bigint      not null,
    author_id  bigint      not null,
    content    text        not null,
    created_at timestamptz not null default now(),

    constraint ck_comment_content_not_blank check (btrim(content) <> ''),

    -- Task 를 지우면 댓글도 함께 사라진다
    constraint fk_comment_task foreign key (task_id)
        references task (id) on delete cascade,
    -- 작성자 없는 댓글은 만들 수 없으므로 사용자 삭제를 막는다
    constraint fk_comment_author foreign key (author_id)
        references app_user (id) on delete restrict
);

create index idx_comment_task_created_at on task_comment (task_id, created_at);
