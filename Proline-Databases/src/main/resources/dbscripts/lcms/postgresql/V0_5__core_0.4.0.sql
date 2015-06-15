
/* SCRIPT GENERATED BY POWER ARCHITECT AND MODIFIED MANUALLY */

ALTER TABLE public.feature_ms2_event DROP CONSTRAINT raw_map_feature_ms2_event_fk;

ALTER TABLE public.scan_sequence RENAME COLUMN raw_file_name TO raw_file_identifier;

ALTER TABLE public.feature_ms2_event RENAME COLUMN run_map_id TO raw_map_id;

-- MANUAL CHANGE: "DEFAULT 0" added
ALTER TABLE public.feature ADD COLUMN peakel_count INTEGER DEFAULT 0 NOT NULL;

-- MANUAL CHANGE: "DEFAULT false" added
ALTER TABLE public.feature_peakel_item ADD COLUMN is_base_peakel BOOLEAN DEFAULT false NOT NULL;

-- MANUAL CHANGE: DROP/ADD INDEX instead of RENAME (order of columns has changed)
-- ALTER INDEX public.feature_moz_time_charge_idx RENAME TO feature_charge_time_moz_idx;
DROP INDEX feature_moz_time_charge_idx;
CREATE INDEX feature_charge_time_moz_idx
 ON public.feature
 ( charge, elution_time, moz);

CREATE INDEX object_tree_schema_name_idx
 ON public.object_tree
 ( schema_name );

CREATE INDEX peakel_time_moz_idx
 ON public.peakel
 ( elution_time, moz );

ALTER TABLE public.feature_ms2_event ADD CONSTRAINT raw_map_feature_ms2_event_fk
FOREIGN KEY (raw_map_id)
REFERENCES public.raw_map (id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

/* END OF SCRIPT GENERATED BY POWER ARCHITECT AND MODIFIED MANUALLY */

/* ADDITIONAL SQL QUERIES USED FOR DATA UPDATE */

-- Enforce that scan_sequence.raw_file_identifier does not contain file extension
UPDATE scan_sequence SET raw_file_identifier = split_part(raw_file_identifier,'.', 1);

-- Update content of COLUMN feature.peakel_count
UPDATE feature SET peakel_count = tuple.peakel_count

FROM (
  SELECT
    feature_peakel_item.feature_id as feature_id,
    count(peakel_id) as peakel_count
  FROM
    feature_peakel_item
  GROUP BY
    feature_peakel_item.feature_id
) tuple

WHERE id = tuple.feature_id;

/* END OF ADDITIONAL SQL QUERIES USED FOR DATA UPDATE */

/* LIST OF OPERATIONS TO BE PERFORMED IN THE NEXT JAVA MIGRATION */
-- Update content of COLUMN feature_peakel_item.is_base_peakel
-- Update peakel serialized binary structure
/* END LIST OF OPERATIONS TO BE PERFORMED IN THE NEXT JAVA MIGRATION */