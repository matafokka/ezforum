--
-- PostgreSQL database dump
--

-- Dumped from database version 11.1
-- Dumped by pg_dump version 11.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: postgres; Type: DATABASE; Schema: -; Owner: matafokka
--

CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'English_United States.1251' LC_CTYPE = 'English_United States.1251';


ALTER DATABASE postgres OWNER TO matafokka;

\connect postgres

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: ezforum; Type: SCHEMA; Schema: -; Owner: matafokka
--

CREATE SCHEMA ezforum;


ALTER SCHEMA ezforum OWNER TO matafokka;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: complaints; Type: TABLE; Schema: ezforum; Owner: matafokka
--

CREATE TABLE ezforum.complaints (
    id bigint NOT NULL,
    usr bigint NOT NULL,
    reason character varying NOT NULL,
    sender bigint NOT NULL
);


ALTER TABLE ezforum.complaints OWNER TO matafokka;

--
-- Name: ban_requests_id_seq; Type: SEQUENCE; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ezforum.complaints ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ezforum.ban_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: post_review_requests; Type: TABLE; Schema: ezforum; Owner: matafokka
--

CREATE TABLE ezforum.post_review_requests (
    post bigint NOT NULL,
    sender bigint NOT NULL,
    reason character varying NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE ezforum.post_review_requests OWNER TO matafokka;

--
-- Name: post_review_request_id_seq; Type: SEQUENCE; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ezforum.post_review_requests ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ezforum.post_review_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: posts; Type: TABLE; Schema: ezforum; Owner: matafokka
--

CREATE TABLE ezforum.posts (
    id bigint NOT NULL,
    topic bigint NOT NULL,
    author bigint NOT NULL,
    text character varying NOT NULL,
    date timestamp without time zone
);


ALTER TABLE ezforum.posts OWNER TO matafokka;

--
-- Name: posts_id_seq; Type: SEQUENCE; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ezforum.posts ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ezforum.posts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: sections; Type: TABLE; Schema: ezforum; Owner: matafokka
--

CREATE TABLE ezforum.sections (
    id bigint NOT NULL,
    name character varying NOT NULL
);


ALTER TABLE ezforum.sections OWNER TO matafokka;

--
-- Name: sections_id_seq; Type: SEQUENCE; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ezforum.sections ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ezforum.sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: subsections; Type: TABLE; Schema: ezforum; Owner: matafokka
--

CREATE TABLE ezforum.subsections (
    id bigint NOT NULL,
    section bigint NOT NULL,
    name character varying NOT NULL
);


ALTER TABLE ezforum.subsections OWNER TO matafokka;

--
-- Name: subsections_id_seq; Type: SEQUENCE; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ezforum.subsections ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ezforum.subsections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: topic_review_requests; Type: TABLE; Schema: ezforum; Owner: matafokka
--

CREATE TABLE ezforum.topic_review_requests (
    topic bigint NOT NULL,
    sender bigint NOT NULL,
    reason character varying NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE ezforum.topic_review_requests OWNER TO matafokka;

--
-- Name: topic_review_requests_id_seq; Type: SEQUENCE; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ezforum.topic_review_requests ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ezforum.topic_review_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: topics; Type: TABLE; Schema: ezforum; Owner: matafokka
--

CREATE TABLE ezforum.topics (
    id bigint NOT NULL,
    subsection bigint NOT NULL,
    author bigint NOT NULL,
    name character varying NOT NULL,
    text character varying NOT NULL,
    date timestamp without time zone,
    locked_by bigint
);


ALTER TABLE ezforum.topics OWNER TO matafokka;

--
-- Name: topics_id_seq; Type: SEQUENCE; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ezforum.topics ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ezforum.topics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: users; Type: TABLE; Schema: ezforum; Owner: matafokka
--

CREATE TABLE ezforum.users (
    id bigint NOT NULL,
    name character varying NOT NULL,
    rank smallint DEFAULT 4 NOT NULL,
    real_name character varying,
    ban_reason character varying,
    email bytea NOT NULL,
    password bytea NOT NULL,
    theme character varying,
    locale character varying
);


ALTER TABLE ezforum.users OWNER TO matafokka;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ezforum.users ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ezforum.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: complaints ban_requests_pk; Type: CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.complaints
    ADD CONSTRAINT ban_requests_pk PRIMARY KEY (id);


--
-- Name: post_review_requests post_review_request_pk; Type: CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.post_review_requests
    ADD CONSTRAINT post_review_request_pk PRIMARY KEY (id);


--
-- Name: posts posts_pk; Type: CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.posts
    ADD CONSTRAINT posts_pk PRIMARY KEY (id);


--
-- Name: sections sections_pk; Type: CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.sections
    ADD CONSTRAINT sections_pk PRIMARY KEY (id);


--
-- Name: subsections subsections_pk; Type: CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.subsections
    ADD CONSTRAINT subsections_pk PRIMARY KEY (id);


--
-- Name: topic_review_requests topic_review_requests_pk; Type: CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.topic_review_requests
    ADD CONSTRAINT topic_review_requests_pk PRIMARY KEY (id);


--
-- Name: topics topics_pk; Type: CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.topics
    ADD CONSTRAINT topics_pk PRIMARY KEY (id);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.users
    ADD CONSTRAINT users_pk PRIMARY KEY (id);


--
-- Name: complaints_sorting; Type: INDEX; Schema: ezforum; Owner: matafokka
--

CREATE UNIQUE INDEX complaints_sorting ON ezforum.complaints USING btree (id DESC NULLS LAST);


--
-- Name: post_review_requests_sorting; Type: INDEX; Schema: ezforum; Owner: matafokka
--

CREATE UNIQUE INDEX post_review_requests_sorting ON ezforum.post_review_requests USING btree (id DESC NULLS LAST);


--
-- Name: posts_sorting; Type: INDEX; Schema: ezforum; Owner: matafokka
--

CREATE INDEX posts_sorting ON ezforum.posts USING btree (date DESC NULLS LAST);


--
-- Name: topic_review_requests_id_idx; Type: INDEX; Schema: ezforum; Owner: matafokka
--

CREATE UNIQUE INDEX topic_review_requests_id_idx ON ezforum.topic_review_requests USING btree (id DESC NULLS LAST);


--
-- Name: topics_sorting; Type: INDEX; Schema: ezforum; Owner: matafokka
--

CREATE INDEX topics_sorting ON ezforum.topics USING btree (date DESC NULLS LAST);


--
-- Name: users_sorting; Type: INDEX; Schema: ezforum; Owner: matafokka
--

CREATE INDEX users_sorting ON ezforum.users USING btree (name DESC NULLS LAST);


--
-- Name: complaints ban_requests_users_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.complaints
    ADD CONSTRAINT ban_requests_users_fk FOREIGN KEY (usr) REFERENCES ezforum.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: complaints complaints_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.complaints
    ADD CONSTRAINT complaints_fk FOREIGN KEY (sender) REFERENCES ezforum.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: post_review_requests post_review_request_posts_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.post_review_requests
    ADD CONSTRAINT post_review_request_posts_fk FOREIGN KEY (post) REFERENCES ezforum.posts(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: post_review_requests post_review_request_users_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.post_review_requests
    ADD CONSTRAINT post_review_request_users_fk FOREIGN KEY (sender) REFERENCES ezforum.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: posts posts_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.posts
    ADD CONSTRAINT posts_fk FOREIGN KEY (author) REFERENCES ezforum.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: posts posts_topics_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.posts
    ADD CONSTRAINT posts_topics_fk FOREIGN KEY (topic) REFERENCES ezforum.topics(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subsections subsections_sections_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.subsections
    ADD CONSTRAINT subsections_sections_fk FOREIGN KEY (section) REFERENCES ezforum.sections(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: topic_review_requests topic_review_requests_topics_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.topic_review_requests
    ADD CONSTRAINT topic_review_requests_topics_fk FOREIGN KEY (topic) REFERENCES ezforum.topics(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: topic_review_requests topic_review_requests_users_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.topic_review_requests
    ADD CONSTRAINT topic_review_requests_users_fk FOREIGN KEY (sender) REFERENCES ezforum.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: topics topics_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.topics
    ADD CONSTRAINT topics_fk FOREIGN KEY (author) REFERENCES ezforum.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: topics topics_locked_by_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.topics
    ADD CONSTRAINT topics_locked_by_fk FOREIGN KEY (locked_by) REFERENCES ezforum.users(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: topics topics_subsections_fk; Type: FK CONSTRAINT; Schema: ezforum; Owner: matafokka
--

ALTER TABLE ONLY ezforum.topics
    ADD CONSTRAINT topics_subsections_fk FOREIGN KEY (subsection) REFERENCES ezforum.subsections(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

