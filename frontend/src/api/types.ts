// =====================================================================
// Mini Jira API 계약
//
// 프론트엔드와 백엔드의 유일한 계약이다. 백엔드 DTO(Kotlin)를 여기에 맞춰 만든다.
// 마크다운 문서와 달리 이 파일은 프론트엔드가 실제로 쓰는 코드라서
// 서버 응답과 어긋나면 조용히 넘어가지 않는다.
// =====================================================================

/** 서버는 UUID 를 문자열로 직렬화한다. */
export type Id = string

/**
 * ISO-8601 UTC.
 * 서버가 Instant 를 쓰므로 항상 'Z' 로 끝난다. 예) 2026-08-31T07:21:55Z
 * jOOQ 는 OffsetDateTime 으로 생성되므로 DTO 로 옮길 때 .toInstant() 를 거친다.
 */
export type IsoInstant = string


// ---------------------------------------------------------------------
// enum
//   서버 enum 이름과 문자열이 같아야 한다 (@Enumerated(EnumType.STRING)).
//   as const 배열로 두면 타입과 '순회 가능한 목록' 을 한 번에 얻는다.
//   (필터 드롭다운을 만들 때 목록이 필요하다)
// ---------------------------------------------------------------------

export const TASK_STATUSES = ['TODO', 'IN_PROGRESS', 'DONE'] as const
export type TaskStatus = (typeof TASK_STATUSES)[number]

export const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH'] as const
export type Priority = (typeof PRIORITIES)[number]

/** 화면 표시용 한글 라벨. 서버 enum 의 label 과 같은 값을 둔다. */
export const STATUS_LABEL: Record<TaskStatus, string> = {
    TODO: '할 일',
    IN_PROGRESS: '진행 중',
    DONE: '완료',
}

export const PRIORITY_LABEL: Record<Priority, string> = {
    LOW: '낮음',
    MEDIUM: '보통',
    HIGH: '높음',
}


// ---------------------------------------------------------------------
// 공통
// ---------------------------------------------------------------------

/**
 * 페이징 응답 래퍼.
 *
 * Spring Data 의 Page 를 그대로 직렬화하지 않고 직접 정의한다.
 * PageImpl 직렬화는 Boot 가 경고를 내고, 구조가 버전에 따라 바뀔 수 있다.
 * 이 모양이 TanStack Query 의 페이지 이동 코드를 결정하므로 여기서 못 박는다.
 */
export interface Page<T> {
    content: T[]
    /** 0-based */
    page: number
    size: number
    totalElements: number
    totalPages: number
}

export interface FieldError {
    field: string
    message: string
}

export interface ApiError {
    /** 화면 분기용 기계 판독 코드. 예) TASK_NOT_FOUND, VALIDATION_FAILED */
    code: string
    /** 사용자에게 그대로 보여줄 수 있는 문장 */
    message: string
    /** 폼 검증 실패일 때만 채워진다 */
    fieldErrors?: FieldError[]
}


// ---------------------------------------------------------------------
// User
// ---------------------------------------------------------------------

/**
 * 담당자 선택과 아바타 표시에 필요한 최소 정보.
 * email 은 화면에서 쓰지 않으므로 응답에 넣지 않는다.
 */
export interface UserSummary {
    id: Id
    name: string
    /** #RRGGBB — 이니셜 아바타 배경색 */
    avatarColor: string
}


// ---------------------------------------------------------------------
// Task
// ---------------------------------------------------------------------

/**
 * 목록 행.
 * description 은 목록에서 쓰지 않으므로 넣지 않는다 — 25건이든 2500건이든
 * 목록 응답에 본문을 싣지 않는 습관이 중요하다.
 */
export interface TaskListItem {
    id: Id
    title: string
    status: TaskStatus
    priority: Priority
    /** null = 담당자 미지정 */
    assignee: UserSummary | null
    /** jOOQ 서브쿼리로 계산한다 */
    commentCount: number
    createdAt: IsoInstant
    updatedAt: IsoInstant
}

export interface TaskDetail {
    id: Id
    title: string
    description: string | null
    status: TaskStatus
    priority: Priority
    assignee: UserSummary | null
    createdAt: IsoInstant
    updatedAt: IsoInstant
}

export interface CreateTaskRequest {
    title: string
    description?: string | null
    /** 생략 시 TODO */
    status?: TaskStatus
    /** 생략 시 MEDIUM */
    priority?: Priority
    assigneeId?: Id | null
}

/**
 * PATCH — 보낸 필드만 바뀐다.
 * status 가 빠져 있는 것은 의도적이다. 상태 변경은 전용 엔드포인트를 쓴다.
 */
export interface UpdateTaskRequest {
    title?: string
    description?: string | null
    priority?: Priority
    assigneeId?: Id | null
}

export interface ChangeStatusRequest {
    status: TaskStatus
}

export const TASK_SORTS = [
    'createdAt,desc',
    'createdAt,asc',
    'updatedAt,desc',
    'priority,desc',
] as const
export type TaskSort = (typeof TASK_SORTS)[number]

/**
 * 목록 조회 조건.
 *
 * 이 객체를 URL search params 와 1:1 로 대응시키고, 그대로 queryKey 에 넣는다.
 *   URL       /tasks?page=1&status=TODO
 *   queryKey  ['tasks', { page: 1, size: 20, status: 'TODO', ... }]
 * 이렇게 두면 '뒤로 가기' 와 캐시가 자동으로 맞아떨어진다.
 */
export interface TaskListQuery {
    page: number
    size: number
    status: TaskStatus | null
    assigneeId: Id | null
    keyword: string | null
    sort: TaskSort
}

export const DEFAULT_TASK_LIST_QUERY: TaskListQuery = {
    page: 0,
    size: 20,
    status: null,
    assigneeId: null,
    keyword: null,
    sort: 'createdAt,desc',
}


// ---------------------------------------------------------------------
// Comment
// ---------------------------------------------------------------------

export interface Comment {
    id: Id
    taskId: Id
    author: UserSummary
    content: string
    createdAt: IsoInstant
}

export interface CreateCommentRequest {
    /** 로그인이 없으므로 작성자를 클라이언트가 지정한다 */
    authorId: Id
    content: string
}
