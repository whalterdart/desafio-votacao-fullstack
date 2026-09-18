CREATE TABLE agendas (
    id UUID PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE voting_sessions (
    id UUID PRIMARY KEY,
    agenda_id UUID NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closes_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_session_agenda FOREIGN KEY (agenda_id) REFERENCES agendas(id),
    CONSTRAINT uk_session_agenda UNIQUE (agenda_id),
    CONSTRAINT ck_session_period CHECK (closes_at > opened_at)
);

CREATE TABLE votes (
    id UUID PRIMARY KEY,
    agenda_id UUID NOT NULL,
    associate_id VARCHAR(80) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    choice VARCHAR(3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_vote_agenda FOREIGN KEY (agenda_id) REFERENCES agendas(id),
    CONSTRAINT uk_vote_agenda_associate UNIQUE (agenda_id, associate_id),
    CONSTRAINT ck_vote_choice CHECK (choice IN ('YES', 'NO'))
);

CREATE INDEX idx_agenda_created_at ON agendas(created_at DESC);
CREATE INDEX idx_vote_agenda_choice ON votes(agenda_id, choice);

