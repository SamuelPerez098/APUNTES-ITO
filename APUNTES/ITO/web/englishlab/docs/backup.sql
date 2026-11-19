--
-- PostgreSQL database dump
--
-- Volcado SOLO DE ESQUEMA (sin datos): la revisión de seguridad determinó no
-- versionar hashes de contraseñas ni datos de usuarios. Los datos demo se
-- restauran con Flyway (V10-V12). El respaldo COMPLETO con datos se genera
-- con pg_dump en el VPS y se guarda fuera del repositorio.

\restrict n1xjXTqXCPmq6jo0OyjLiDB6imNBf7FuNoMmYnNNFNeAaQAvLVI5SX52eMFQT2D

-- Dumped from database version 16.14 (Ubuntu 16.14-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.14 (Ubuntu 16.14-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_teacher_id_fkey;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_role_id_fkey;
ALTER TABLE IF EXISTS ONLY public.revoked_tokens DROP CONSTRAINT IF EXISTS revoked_tokens_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.objective_prerequisites DROP CONSTRAINT IF EXISTS objective_prerequisites_prerequisite_id_fkey;
ALTER TABLE IF EXISTS ONLY public.objective_prerequisites DROP CONSTRAINT IF EXISTS objective_prerequisites_objective_id_fkey;
ALTER TABLE IF EXISTS ONLY public.notifications_log DROP CONSTRAINT IF EXISTS notifications_log_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.learner_skill_state DROP CONSTRAINT IF EXISTS learner_skill_state_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.learner_skill_state DROP CONSTRAINT IF EXISTS learner_skill_state_objective_id_fkey;
ALTER TABLE IF EXISTS ONLY public.attempts DROP CONSTRAINT IF EXISTS attempts_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.attempts DROP CONSTRAINT IF EXISTS attempts_assignment_id_fkey;
ALTER TABLE IF EXISTS ONLY public.attempts DROP CONSTRAINT IF EXISTS attempts_activity_id_fkey;
ALTER TABLE IF EXISTS ONLY public.assignments DROP CONSTRAINT IF EXISTS assignments_teacher_id_fkey;
ALTER TABLE IF EXISTS ONLY public.assignments DROP CONSTRAINT IF EXISTS assignments_student_id_fkey;
ALTER TABLE IF EXISTS ONLY public.assignments DROP CONSTRAINT IF EXISTS assignments_activity_id_fkey;
ALTER TABLE IF EXISTS ONLY public.activities DROP CONSTRAINT IF EXISTS activities_objective_id_fkey;
ALTER TABLE IF EXISTS ONLY public.activities DROP CONSTRAINT IF EXISTS activities_category_id_fkey;
DROP INDEX IF EXISTS public.ux_users_email;
DROP INDEX IF EXISTS public.ux_objectives_code;
DROP INDEX IF EXISTS public.ix_users_teacher_id;
DROP INDEX IF EXISTS public.ix_users_role_id;
DROP INDEX IF EXISTS public.ix_revoked_user_id;
DROP INDEX IF EXISTS public.ix_revoked_expires_at;
DROP INDEX IF EXISTS public.ix_prt_user_id;
DROP INDEX IF EXISTS public.ix_prt_token_hash;
DROP INDEX IF EXISTS public.ix_objprereq_prerequisite;
DROP INDEX IF EXISTS public.ix_notif_user;
DROP INDEX IF EXISTS public.ix_lss_objective;
DROP INDEX IF EXISTS public.ix_attempts_user_activity;
DROP INDEX IF EXISTS public.ix_attempts_user;
DROP INDEX IF EXISTS public.ix_attempts_assignment;
DROP INDEX IF EXISTS public.ix_assignments_teacher;
DROP INDEX IF EXISTS public.ix_assignments_student;
DROP INDEX IF EXISTS public.ix_assignments_activity;
DROP INDEX IF EXISTS public.ix_activities_type;
DROP INDEX IF EXISTS public.ix_activities_skill;
DROP INDEX IF EXISTS public.ix_activities_objective_id;
DROP INDEX IF EXISTS public.ix_activities_level;
DROP INDEX IF EXISTS public.ix_activities_category_id;
DROP INDEX IF EXISTS public.flyway_schema_history_s_idx;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.roles DROP CONSTRAINT IF EXISTS roles_pkey;
ALTER TABLE IF EXISTS ONLY public.roles DROP CONSTRAINT IF EXISTS roles_name_key;
ALTER TABLE IF EXISTS ONLY public.revoked_tokens DROP CONSTRAINT IF EXISTS revoked_tokens_pkey;
ALTER TABLE IF EXISTS ONLY public.revoked_tokens DROP CONSTRAINT IF EXISTS revoked_tokens_jti_key;
ALTER TABLE IF EXISTS ONLY public.password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_pkey;
ALTER TABLE IF EXISTS ONLY public.objectives DROP CONSTRAINT IF EXISTS objectives_pkey;
ALTER TABLE IF EXISTS ONLY public.objective_prerequisites DROP CONSTRAINT IF EXISTS objective_prerequisites_pkey;
ALTER TABLE IF EXISTS ONLY public.notifications_log DROP CONSTRAINT IF EXISTS notifications_log_pkey;
ALTER TABLE IF EXISTS ONLY public.learner_skill_state DROP CONSTRAINT IF EXISTS learner_skill_state_pkey;
ALTER TABLE IF EXISTS ONLY public.flyway_schema_history DROP CONSTRAINT IF EXISTS flyway_schema_history_pk;
ALTER TABLE IF EXISTS ONLY public.categories DROP CONSTRAINT IF EXISTS categories_pkey;
ALTER TABLE IF EXISTS ONLY public.categories DROP CONSTRAINT IF EXISTS categories_name_key;
ALTER TABLE IF EXISTS ONLY public.attempts DROP CONSTRAINT IF EXISTS attempts_pkey;
ALTER TABLE IF EXISTS ONLY public.assignments DROP CONSTRAINT IF EXISTS assignments_pkey;
ALTER TABLE IF EXISTS ONLY public.activities DROP CONSTRAINT IF EXISTS activities_pkey;
DROP TABLE IF EXISTS public.users;
DROP TABLE IF EXISTS public.roles;
DROP TABLE IF EXISTS public.revoked_tokens;
DROP TABLE IF EXISTS public.password_reset_tokens;
DROP TABLE IF EXISTS public.objectives;
DROP TABLE IF EXISTS public.objective_prerequisites;
DROP TABLE IF EXISTS public.notifications_log;
DROP TABLE IF EXISTS public.learner_skill_state;
DROP TABLE IF EXISTS public.flyway_schema_history;
DROP TABLE IF EXISTS public.categories;
DROP TABLE IF EXISTS public.attempts;
DROP TABLE IF EXISTS public.assignments;
DROP TABLE IF EXISTS public.activities;
-- *not* dropping schema, since initdb creates it
--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS '';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: activities; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.activities (
    id bigint NOT NULL,
    objective_id bigint NOT NULL,
    type character varying(20) NOT NULL,
    level character varying(2) NOT NULL,
    skill character varying(20) NOT NULL,
    title character varying(200) NOT NULL,
    content jsonb NOT NULL,
    category_id bigint,
    CONSTRAINT ck_activities_level CHECK (((level)::text = ANY ((ARRAY['A1'::character varying, 'A2'::character varying, 'B1'::character varying, 'B2'::character varying])::text[]))),
    CONSTRAINT ck_activities_type CHECK (((type)::text = ANY ((ARRAY['MULTIPLE_CHOICE'::character varying, 'FILL_BLANK'::character varying])::text[])))
);


--
-- Name: activities_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.activities ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.activities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: assignments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.assignments (
    id bigint NOT NULL,
    teacher_id bigint NOT NULL,
    student_id bigint NOT NULL,
    activity_id bigint NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    assigned_at timestamp with time zone DEFAULT now() NOT NULL,
    completed_at timestamp with time zone,
    CONSTRAINT ck_assignments_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'COMPLETED'::character varying])::text[])))
);


--
-- Name: assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.assignments ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.assignments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: attempts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attempts (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    activity_id bigint NOT NULL,
    assignment_id bigint,
    user_answer text NOT NULL,
    is_correct boolean NOT NULL,
    score numeric(5,4) NOT NULL,
    attempted_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: attempts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.attempts ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.attempts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categories (
    id bigint NOT NULL,
    name character varying(120) NOT NULL,
    description text
);


--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.categories ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: learner_skill_state; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.learner_skill_state (
    user_id bigint NOT NULL,
    objective_id bigint NOT NULL,
    mastery numeric(5,4) DEFAULT 0.3000 NOT NULL,
    attempts_count integer DEFAULT 0 NOT NULL,
    correct_count integer DEFAULT 0 NOT NULL,
    last_attempt_at timestamp with time zone
);


--
-- Name: notifications_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications_log (
    id bigint NOT NULL,
    user_id bigint,
    channel character varying(20) NOT NULL,
    type character varying(30) NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    error_message text,
    sent_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT ck_notif_channel CHECK (((channel)::text = ANY ((ARRAY['EMAIL'::character varying, 'SMS'::character varying, 'WHATSAPP'::character varying])::text[]))),
    CONSTRAINT ck_notif_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying])::text[])))
);


--
-- Name: notifications_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.notifications_log ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.notifications_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: objective_prerequisites; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.objective_prerequisites (
    objective_id bigint NOT NULL,
    prerequisite_id bigint NOT NULL,
    CONSTRAINT ck_objprereq_distinct CHECK ((objective_id <> prerequisite_id))
);


--
-- Name: objectives; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.objectives (
    id bigint NOT NULL,
    code character varying(10) NOT NULL,
    name character varying(150) NOT NULL,
    cefr_level character varying(2) NOT NULL,
    skill character varying(20) NOT NULL,
    description text,
    CONSTRAINT ck_objectives_cefr_level CHECK (((cefr_level)::text = ANY ((ARRAY['A1'::character varying, 'A2'::character varying, 'B1'::character varying, 'B2'::character varying])::text[])))
);


--
-- Name: objectives_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.objectives ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.objectives_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_tokens (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    token_hash character varying(64) NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    used_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.password_reset_tokens ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.password_reset_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: revoked_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.revoked_tokens (
    id bigint NOT NULL,
    jti character varying(64) NOT NULL,
    user_id bigint NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: revoked_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.revoked_tokens ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.revoked_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    name character varying(20) NOT NULL,
    CONSTRAINT ck_roles_name CHECK (((name)::text = ANY ((ARRAY['ADMIN'::character varying, 'TEACHER'::character varying, 'STUDENT'::character varying])::text[])))
);


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.roles ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    name character varying(120) NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(100) NOT NULL,
    role_id bigint NOT NULL,
    teacher_id bigint,
    phone character varying(20),
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: activities activities_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activities
    ADD CONSTRAINT activities_pkey PRIMARY KEY (id);


--
-- Name: assignments assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_pkey PRIMARY KEY (id);


--
-- Name: attempts attempts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attempts
    ADD CONSTRAINT attempts_pkey PRIMARY KEY (id);


--
-- Name: categories categories_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_name_key UNIQUE (name);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: learner_skill_state learner_skill_state_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.learner_skill_state
    ADD CONSTRAINT learner_skill_state_pkey PRIMARY KEY (user_id, objective_id);


--
-- Name: notifications_log notifications_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications_log
    ADD CONSTRAINT notifications_log_pkey PRIMARY KEY (id);


--
-- Name: objective_prerequisites objective_prerequisites_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objective_prerequisites
    ADD CONSTRAINT objective_prerequisites_pkey PRIMARY KEY (objective_id, prerequisite_id);


--
-- Name: objectives objectives_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objectives
    ADD CONSTRAINT objectives_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: revoked_tokens revoked_tokens_jti_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.revoked_tokens
    ADD CONSTRAINT revoked_tokens_jti_key UNIQUE (jti);


--
-- Name: revoked_tokens revoked_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.revoked_tokens
    ADD CONSTRAINT revoked_tokens_pkey PRIMARY KEY (id);


--
-- Name: roles roles_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: ix_activities_category_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_activities_category_id ON public.activities USING btree (category_id);


--
-- Name: ix_activities_level; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_activities_level ON public.activities USING btree (level);


--
-- Name: ix_activities_objective_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_activities_objective_id ON public.activities USING btree (objective_id);


--
-- Name: ix_activities_skill; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_activities_skill ON public.activities USING btree (skill);


--
-- Name: ix_activities_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_activities_type ON public.activities USING btree (type);


--
-- Name: ix_assignments_activity; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_assignments_activity ON public.assignments USING btree (activity_id);


--
-- Name: ix_assignments_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_assignments_student ON public.assignments USING btree (student_id);


--
-- Name: ix_assignments_teacher; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_assignments_teacher ON public.assignments USING btree (teacher_id);


--
-- Name: ix_attempts_assignment; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_attempts_assignment ON public.attempts USING btree (assignment_id);


--
-- Name: ix_attempts_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_attempts_user ON public.attempts USING btree (user_id);


--
-- Name: ix_attempts_user_activity; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_attempts_user_activity ON public.attempts USING btree (user_id, activity_id);


--
-- Name: ix_lss_objective; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_lss_objective ON public.learner_skill_state USING btree (objective_id);


--
-- Name: ix_notif_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_notif_user ON public.notifications_log USING btree (user_id);


--
-- Name: ix_objprereq_prerequisite; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_objprereq_prerequisite ON public.objective_prerequisites USING btree (prerequisite_id);


--
-- Name: ix_prt_token_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_prt_token_hash ON public.password_reset_tokens USING btree (token_hash);


--
-- Name: ix_prt_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_prt_user_id ON public.password_reset_tokens USING btree (user_id);


--
-- Name: ix_revoked_expires_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_revoked_expires_at ON public.revoked_tokens USING btree (expires_at);


--
-- Name: ix_revoked_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_revoked_user_id ON public.revoked_tokens USING btree (user_id);


--
-- Name: ix_users_role_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_users_role_id ON public.users USING btree (role_id);


--
-- Name: ix_users_teacher_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_users_teacher_id ON public.users USING btree (teacher_id);


--
-- Name: ux_objectives_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_objectives_code ON public.objectives USING btree (code);


--
-- Name: ux_users_email; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_users_email ON public.users USING btree (lower((email)::text));


--
-- Name: activities activities_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activities
    ADD CONSTRAINT activities_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE SET NULL;


--
-- Name: activities activities_objective_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activities
    ADD CONSTRAINT activities_objective_id_fkey FOREIGN KEY (objective_id) REFERENCES public.objectives(id) ON DELETE CASCADE;


--
-- Name: assignments assignments_activity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_activity_id_fkey FOREIGN KEY (activity_id) REFERENCES public.activities(id) ON DELETE CASCADE;


--
-- Name: assignments assignments_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: assignments assignments_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: attempts attempts_activity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attempts
    ADD CONSTRAINT attempts_activity_id_fkey FOREIGN KEY (activity_id) REFERENCES public.activities(id) ON DELETE CASCADE;


--
-- Name: attempts attempts_assignment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attempts
    ADD CONSTRAINT attempts_assignment_id_fkey FOREIGN KEY (assignment_id) REFERENCES public.assignments(id) ON DELETE SET NULL;


--
-- Name: attempts attempts_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attempts
    ADD CONSTRAINT attempts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: learner_skill_state learner_skill_state_objective_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.learner_skill_state
    ADD CONSTRAINT learner_skill_state_objective_id_fkey FOREIGN KEY (objective_id) REFERENCES public.objectives(id) ON DELETE CASCADE;


--
-- Name: learner_skill_state learner_skill_state_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.learner_skill_state
    ADD CONSTRAINT learner_skill_state_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: notifications_log notifications_log_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications_log
    ADD CONSTRAINT notifications_log_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: objective_prerequisites objective_prerequisites_objective_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objective_prerequisites
    ADD CONSTRAINT objective_prerequisites_objective_id_fkey FOREIGN KEY (objective_id) REFERENCES public.objectives(id) ON DELETE CASCADE;


--
-- Name: objective_prerequisites objective_prerequisites_prerequisite_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objective_prerequisites
    ADD CONSTRAINT objective_prerequisites_prerequisite_id_fkey FOREIGN KEY (prerequisite_id) REFERENCES public.objectives(id) ON DELETE CASCADE;


--
-- Name: password_reset_tokens password_reset_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: revoked_tokens revoked_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.revoked_tokens
    ADD CONSTRAINT revoked_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: users users_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: users users_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: -
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


--
-- PostgreSQL database dump complete
--

\unrestrict n1xjXTqXCPmq6jo0OyjLiDB6imNBf7FuNoMmYnNNFNeAaQAvLVI5SX52eMFQT2D

