ALTER TABLE alarm.alarm_all 
DROP CONSTRAINT IF EXISTS uk_alarm_nbi_id_ne;

-- Tạo UNIQUE constraint mới
ALTER TABLE alarm.alarm_all 
ADD CONSTRAINT uk_alarm_nbi_id_ne 
UNIQUE (nbi_alarm_id, ne);