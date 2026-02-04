-- ============================================================
-- Stored Procedure: update_alarm_end
-- Description: Update alarm record when END record is received
--              If no ACTIVE record found, INSERT new record with CLEARED status
-- ============================================================

CREATE OR REPLACE PROCEDURE alarm.update_alarm_all(
    p_nbi_alarm_id VARCHAR(255),
    p_ne VARCHAR(255),
    
    -- Thông tin alarm
    p_nbi_alarm_type VARCHAR(100),
    p_nbi_perceived_severity VARCHAR(100),
    p_nbi_specific_problem text,
    p_nbi_additional_text text,
    p_nbi_object_instance VARCHAR(200),
    
    -- Thông tin thiết bị/ vị trí
    p_cell_id VARCHAR(100),
    p_site VARCHAR(100),
    p_ne_type VARCHAR(50),
    p_ip_address VARCHAR(100),
    
    -- Thông tin thời gian
	p_nbi_alarm_time VARCHAR(100),
    p_nbi_clear_time VARCHAR(100),
    p_record_type VARCHAR(20),
    
    -- Thông tin địa lý
    p_network VARCHAR(50),
    p_region VARCHAR(100),
    p_province VARCHAR(100),
    p_district VARCHAR(100),
    p_dept VARCHAR(100),
    p_team VARCHAR(100),

	-- Thời gian nhận
    p_tg_nhan VARCHAR(100)
)
LANGUAGE plpgsql
AS $$
DECLARE
	v_parsed_alarm_time TIMESTAMP;
	v_parsed_clear_time TIMESTAMP;
    v_affected_rows INTEGER;
BEGIN
	v_parsed_alarm_time = CASE 
                WHEN p_nbi_alarm_time IS NULL OR TRIM(p_nbi_alarm_time) = '' THEN NULL
                ELSE TO_TIMESTAMP(p_nbi_alarm_time, 'YYYY-MM-DD,HH24:MI:SS') END;
	v_parsed_clear_time = CASE 
                WHEN p_nbi_clear_time IS NULL OR TRIM(p_nbi_clear_time) = '' THEN NULL
                ELSE TO_TIMESTAMP(p_nbi_clear_time, 'YYYY-MM-DD,HH24:MI:SS') END;
    
	UPDATE alarm.alarm_all
    SET 
        status = 'CLEARED',
        record_type = p_record_type,
        nbi_clear_time = CASE 
            WHEN p_nbi_clear_time IS NULL OR TRIM(p_nbi_clear_time) = '' THEN NULL
            ELSE TO_TIMESTAMP(p_nbi_clear_time, 'YYYY-MM-DD,HH24:MI:SS') END,
        last_updated = CURRENT_TIMESTAMP
    WHERE nbi_alarm_id = p_nbi_alarm_id AND ne = p_ne AND status = 'ACTIVE';
    
    -- Get number of affected rows
    GET DIAGNOSTICS v_affected_rows = ROW_COUNT;
    
    -- If no active alarm found, INSERT new record with CLEARED status
    -- This handles the case when END arrives before START
    IF v_affected_rows = 0 THEN
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
            'CLEARED',  -- Status is CLEARED (END arrived first)
            p_record_type,
            p_network,
            p_region,
            p_province,
            p_district,
            p_dept,
            p_team,
            p_tg_nhan
        )
		ON CONFLICT (nbi_alarm_id, ne)
        DO UPDATE SET
            -- Update các trường thông tin
			nbi_alarm_time = alarm.alarm_all.nbi_alarm_time,
			nbi_clear_time = CASE
                WHEN alarm.alarm_all.nbi_clear_time IS NULL AND EXCLUDED.nbi_clear_time IS NOT NULL
                THEN EXCLUDED.nbi_clear_time ELSE alarm.alarm_all.nbi_clear_time END,
			status = 'CLEARED',
            record_type = EXCLUDED.record_type,     
            last_updated = CURRENT_TIMESTAMP;
        
    ELSE
        RAISE NOTICE 'Updated alarm % for NE: % to CLEARED status', p_nbi_alarm_id, p_ne;
    END IF;
END;
$$;
