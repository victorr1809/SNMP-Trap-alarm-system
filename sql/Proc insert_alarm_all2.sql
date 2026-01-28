CREATE OR REPLACE PROCEDURE alarm.insert_alarm_all2(
    -- Alarm identifiers
    p_nbi_alarm_id VARCHAR(255),
    p_ne VARCHAR(255),
    
    -- Alarm information
    p_nbi_alarm_type VARCHAR(100),
    p_nbi_perceived_severity VARCHAR(100),
    p_nbi_specific_problem text,
    p_nbi_additional_text text,
    p_nbi_object_instance VARCHAR(200),
    
    -- Device/Location information
    p_cell_id VARCHAR(100),
    p_site VARCHAR(100),
    p_ne_type VARCHAR(50),
    p_ip_address VARCHAR(100),
    
    -- Time information
    p_nbi_alarm_time VARCHAR(100),
    p_nbi_clear_time VARCHAR(100),
    p_record_type VARCHAR(20),
    
    -- Geography information
    p_network VARCHAR(50),
    p_region VARCHAR(100),
    p_province VARCHAR(100),
    p_district VARCHAR(100),
    p_dept VARCHAR(100),
    p_team VARCHAR(100),
    
    -- Processing flags
    p_is_send_sms VARCHAR(100),
    p_tg_nhan VARCHAR(100)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_parsed_alarm_time TIMESTAMP;
    v_parsed_clear_time TIMESTAMP;
BEGIN
	v_parsed_alarm_time = case when p_nbi_alarm_time IS NULL OR TRIM(p_nbi_alarm_time) = '' THEN NULL 
				else TO_TIMESTAMP(p_nbi_alarm_time, 'YYYY-MM-DD,HH24:MI:SS') END;
	v_parsed_clear_time = case when p_nbi_clear_time IS NULL OR TRIM(p_nbi_alarm_time) = '' THEN NULL 
				else TO_TIMESTAMP(p_nbi_clear_time, 'YYYY-MM-DD,HH24:MI:SS') END;

	INSERT INTO alarm.alarm_all (
        nbi_alarm_id,
        ne,
        nbi_alarm_type,
        nbi_perceived_severity,
        nbi_specific_problem,
        nbi_additional_text,
        nbi_object_instance,
        cell_id,
        site,
        ne_type,
        ip_address,
		nbi_alarm_time,
        nbi_clear_time,
        last_updated,
        status,
        record_type,
        network,
        region,
        province,
        district,
        dept,
        team,
        is_send_sms,
        tg_nhan
    ) VALUES (
        p_nbi_alarm_id,
        p_ne,
        p_nbi_alarm_type,
        p_nbi_perceived_severity,
        p_nbi_specific_problem,
        p_nbi_additional_text,
        p_nbi_object_instance,
        p_cell_id,
        p_site,
        p_ne_type,
        p_ip_address,
		v_parsed_alarm_time,  
        v_parsed_clear_time, 
        CURRENT_TIMESTAMP,
        'ACTIVE',             
        p_record_type,
        p_network,
        p_region,
        p_province,
        p_district,
        p_dept,
        p_team,
        p_is_send_sms,
        p_tg_nhan
    ) 
	ON CONFLICT (nbi_alarm_id, ne) 
	DO UPDATE SET
	    -- Update các trường thông tin với data mới nhất
        nbi_specific_problem = EXCLUDED.nbi_specific_problem,
        nbi_additional_text = EXCLUDED.nbi_additional_text,
		nbi_alarm_time = CASE
            WHEN alarm.alarm_all.nbi_alarm_time IS NULL AND EXCLUDED.nbi_alarm_time IS NOT NULL THEN EXCLUDED.nbi_alarm_time
            ELSE alarm.alarm_all.nbi_alarm_time
        END,
        nbi_clear_time = alarm.alarm_all.nbi_clear_time,
        status = CASE
            WHEN alarm.alarm_all.status = 'CLEARED' THEN 'CLEARED'
            ELSE EXCLUDED.status
        END,
        record_type = CASE
            WHEN alarm.alarm_all.status = 'CLEARED' THEN alarm.alarm_all.record_type
            ELSE EXCLUDED.record_type
        END,
        last_updated = CURRENT_TIMESTAMP,
        tg_nhan = EXCLUDED.tg_nhan;
END;
$$