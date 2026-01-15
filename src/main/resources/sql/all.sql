create table admin
(
    id              bigint auto_increment comment '管理员ID'
        primary key,
    username        varchar(50)                        not null comment '管理员账号',
    password        varchar(100)                       not null comment '密码',
    real_name       varchar(50)                        null comment '真实姓名',
    phone           varchar(20)                        null comment '联系电话',
    email           varchar(100)                       null comment '邮箱',
    status          tinyint  default 1                 not null comment '状态：0-禁用，1-启用',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    last_login_time datetime                           null comment '最后登录时间',
    constraint uk_username
        unique (username)
)
    comment '管理员表';


create table chat_message
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    session_id   varchar(64)                        not null comment '会话ID',
    sender_id    bigint                             not null comment '发送者ID',
    receiver_id  bigint                             not null comment '接收者ID',
    message_type tinyint  default 1                 null comment '消息类型:1文本,2图片,3商品卡片',
    content      text                               not null comment '消息内容',
    goods_id     bigint                             null comment '关联商品ID(商品卡片类型)',
    is_read      tinyint  default 0                 null comment '是否已读:0未读,1已读',
    is_recalled  tinyint  default 0                 null comment '是否已撤回:0未撤回,1已撤回',
    send_time    datetime default CURRENT_TIMESTAMP null comment '发送时间'
)
    comment '聊天消息表';

create index idx_receiver
    on chat_message (receiver_id);

create index idx_send_time
    on chat_message (send_time);

create index idx_sender
    on chat_message (sender_id);

create index idx_session
    on chat_message (session_id);



create table chat_session
(
    id                bigint auto_increment comment '主键ID'
        primary key,
    session_id        varchar(64)                        not null comment '会话ID(user1_user2格式,小ID在前)',
    user1_id          bigint                             not null comment '用户1ID',
    user2_id          bigint                             not null comment '用户2ID',
    goods_id          bigint                             null comment '关联商品ID',
    last_message      varchar(500)                       null comment '最后一条消息',
    last_message_time datetime                           null comment '最后消息时间',
    user1_unread      int      default 0                 null comment '用户1未读数',
    user2_unread      int      default 0                 null comment '用户2未读数',
    create_time       datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time       datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    user1_hide        tinyint  default 0                 null comment 'user1是否隐藏会话:0显示,1隐藏',
    user2_hide        tinyint  default 0                 null comment 'user2是否隐藏会话:0显示,1隐藏',
    constraint session_id
        unique (session_id)
)
    comment '聊天会话表';

create index idx_goods
    on chat_session (goods_id);

create index idx_last_message_time
    on chat_session (last_message_time);

create index idx_user1
    on chat_session (user1_id);

create index idx_user2
    on chat_session (user2_id);



create table comments
(
    id               bigint auto_increment comment '评论ID'
        primary key,
    goods_id         bigint                             not null comment '商品ID',
    user_id          bigint                             not null comment '评论用户ID',
    content          text                               not null comment '评论内容',
    parent_id        bigint                             null comment '父评论ID（顶级评论为NULL）',
    reply_to_user_id bigint                             null comment '被回复的用户ID',
    like_count       int      default 0                 null comment '点赞数',
    status           tinyint  default 1                 null comment '评论状态：1-正常，2-已删除，3-违规屏蔽',
    create_time      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '评论表' collate = utf8mb4_unicode_ci;

create index idx_create_time
    on comments (create_time);

create index idx_goods_id
    on comments (goods_id);

create index idx_parent_id
    on comments (parent_id);

create index idx_user_id
    on comments (user_id);

create table goods
(
    id              bigint auto_increment comment '商品ID'
        primary key,
    owner_id        bigint                                   not null comment '卖家ID',
    goods_type      tinyint     default 1                    not null comment '商品类型：1-售卖商品，2-租赁商品',
    description     text                                     not null comment '商品描述',
    image_urls      json                                     null comment '商品图片URL列表（JSON数组）',
    category_id     bigint                                   null comment '商品分类ID',
    condition_level tinyint     default 1                    not null comment '成色：1-全新，2-几乎全新，3-轻微使用痕迹，4-明显使用痕迹',
    collect_num     int         default 0                    not null comment '收藏数',
    status          tinyint     default 1                    not null comment '状态：1-上架，2-已售出，3-租期中，4-已下架，5-违规删除/系统屏蔽',
    sell_price      decimal(10, 2)                           null comment '售卖价格',
    rent_price      decimal(10, 2)                           null comment '租赁价格（每天）',
    cover_url       varchar(500)                             null comment '封面图片URL',
    create_time     datetime(3) default CURRENT_TIMESTAMP(3) null,
    update_time     datetime(3) default CURRENT_TIMESTAMP(3) null on update CURRENT_TIMESTAMP(3)
)
    comment '商品表' collate = utf8mb4_unicode_ci;

create index idx_category_status
    on goods (category_id, status);

create fulltext index idx_description_fulltext
    on goods (description);

create index idx_owner
    on goods (owner_id);

create index idx_update_time
    on goods (update_time desc);



create table goods
(
    id              bigint auto_increment comment '商品ID'
        primary key,
    owner_id        bigint                                   not null comment '卖家ID',
    goods_type      tinyint     default 1                    not null comment '商品类型：1-售卖商品，2-租赁商品',
    description     text                                     not null comment '商品描述',
    image_urls      json                                     null comment '商品图片URL列表（JSON数组）',
    category_id     bigint                                   null comment '商品分类ID',
    condition_level tinyint     default 1                    not null comment '成色：1-全新，2-几乎全新，3-轻微使用痕迹，4-明显使用痕迹',
    collect_num     int         default 0                    not null comment '收藏数',
    status          tinyint     default 1                    not null comment '状态：1-上架，2-已售出，3-租期中，4-已下架，5-违规删除/系统屏蔽',
    sell_price      decimal(10, 2)                           null comment '售卖价格',
    rent_price      decimal(10, 2)                           null comment '租赁价格（每天）',
    cover_url       varchar(500)                             null comment '封面图片URL',
    create_time     datetime(3) default CURRENT_TIMESTAMP(3) null,
    update_time     datetime(3) default CURRENT_TIMESTAMP(3) null on update CURRENT_TIMESTAMP(3)
)
    comment '商品表' collate = utf8mb4_unicode_ci;

create index idx_category_status
    on goods (category_id, status);

create fulltext index idx_description_fulltext
    on goods (description);

create index idx_owner
    on goods (owner_id);

create index idx_update_time
    on goods (update_time desc);


create table user
(
    id          bigint auto_increment comment '用户ID'
        primary key,
    account_num varchar(50)       not null comment '用户名',
    password    varchar(100)      not null comment '密码',
    email       varchar(100)      null comment '邮箱',
    phone       varchar(20)       not null comment '手机号',
    nickname    varchar(50)       null comment '昵称',
    gender      varchar(10)       null comment '性别',
    image       varchar(255)      null comment '头像URL',
    bio         varchar(200)      null comment '个人简介',
    status      tinyint default 1 null comment '状态：0-禁用，1-启用',
    create_time datetime          not null comment '创建时间',
    update_time datetime          not null comment '更新时间',
    constraint uk_phone
        unique (phone),
    constraint uk_username
        unique (account_num)
)
    comment 'user' collate = utf8mb4_unicode_ci;

create index idx_email
    on user (email);