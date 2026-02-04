CREATE TABLE alarm.alarm_all (
    id BIGSERIAL PRIMARY KEY,
    nbi_alarm_id VARCHAR(255) NOT NULL,
    ne VARCHAR(255) NOT NULL,
    
    -- Thông tin alarm
    nbi_alarm_type VARCHAR(100),
    nbi_perceived_severity VARCHAR(100),
    nbi_specific_problem VARCHAR(150),
    nbi_additional_text VARCHAR(150),
    nbi_object_instance VARCHAR(100),
    
    -- Thông tin thiết bị/vị trí
    cell_id VARCHAR(100),
    site VARCHAR(100),
    ne_type VARCHAR(50),
    ip_address VARCHAR(100),

	-- Thời gian
    nbi_alarm_time TIMESTAMP,
    nbi_clear_time TIMESTAMP,
    last_updated TIMESTAMP NOT NULL,
    
    -- Trạng thái
    status VARCHAR(20) DEFAULT 'ACTIVE', -- 'ACTIVE', 'CLEARED', 'ACKNOWLEDGED'
    record_type VARCHAR(20), -- 'START', 'END', 'ACK'
    
    -- Thông tin địa lý
    network VARCHAR(50),
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

-- Indexes quan trọng
-- CREATE INDEX idx_alarms_status ON alarms(status) WHERE status = 'ACTIVE';
-- CREATE INDEX idx_alarms_time ON alarms(nbi_alarm_time DESC);
-- CREATE INDEX idx_alarms_geography ON alarms(province, region, district);
-- CREATE INDEX idx_alarms_ne ON alarms(ne);
-- CREATE INDEX idx_alarms_severity ON alarms(nbi_perceived_severity);
-- CREATE INDEX idx_alarms_composite ON alarms(status, nbi_alarm_time) WHERE status = 'ACTIVE';