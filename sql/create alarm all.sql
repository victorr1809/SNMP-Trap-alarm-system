CREATE TABLE alarm.alarm_all (
    id BIGSERIAL PRIMARY KEY,
    nbi_alarm_id VARCHAR(255) NOT NULL,
    ne VARCHAR(255) NOT NULL,
    
    -- Thông tin alarm
    nbi_alarm_type VARCHAR(100),
    nbi_perceived_severity VARCHAR(100),
    nbi_specific_problem TEXT,
    nbi_additional_text TEXT,
    nbi_object_instance VARCHAR(255),
    
    -- Thông tin thiết bị/vị trí
    cell_id VARCHAR(100),
    site VARCHAR(100),
    ne_type VARCHAR(100),
    ip_address VARCHAR(100),

	-- Thời gian
    nbi_alarm_time TIMESTAMP,
    nbi_clear_time TIMESTAMP,
    last_updated TIMESTAMP NOT NULL,
    
    -- Trạng thái
    status VARCHAR(100) DEFAULT 'ACTIVE', -- 'ACTIVE', 'CLEARED', 'ACKNOWLEDGED'
    record_type VARCHAR(100), -- 'START', 'END', 'ACK'
    
    -- Thông tin địa lý
    network VARCHAR(100),
    region VARCHAR(100),
    province VARCHAR(100),
    district VARCHAR(100),
    dept VARCHAR(100),
    team VARCHAR(100),

	-- Thời gian nhận
    tg_nhan VARCHAR(100)
    
    -- -- Composite unique constraint
    -- CONSTRAINT uk_alarm UNIQUE (nbi_alarm_id, ne)
);