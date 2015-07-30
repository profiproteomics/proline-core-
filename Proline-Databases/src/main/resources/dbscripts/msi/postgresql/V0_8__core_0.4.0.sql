
/* SCRIPT GENERATED BY POWER ARCHITECT AND MODIFIED MANUALLY */

ALTER TABLE public.protein_set DROP CONSTRAINT protein_match_protein_set_fk;

ALTER TABLE ONLY public.protein_match ALTER COLUMN score TYPE REAL, ALTER COLUMN score SET NOT NULL;

ALTER TABLE public.protein_set_protein_match_item ADD COLUMN coverage REAL DEFAULT 0 NOT NULL;

ALTER TABLE public.search_settings DROP COLUMN quantitation;

ALTER TABLE public.peaklist RENAME COLUMN raw_file_name TO raw_file_identifier;

-- MANUAL CHANGE: "NOT NULL" constraint removed (re-added after data migration)
ALTER TABLE public.peptide_set ADD COLUMN sequence_count INTEGER;

ALTER TABLE public.protein_set RENAME COLUMN typical_protein_match_id TO representative_protein_match_id;

ALTER TABLE public.msi_search DROP COLUMN submitted_queries_count;

CREATE INDEX peptide_match_best_child_idx
 ON public.peptide_match
 ( best_child_id );

CREATE INDEX protein_set_master_quant_component_idx
 ON public.protein_set
 ( master_quant_component_id );

CREATE INDEX peptide_match_relation_parent_peptide_match_idx
 ON public.peptide_match_relation
 ( parent_peptide_match_id );

CREATE INDEX peptide_instance_peptide_match_map_peptide_match_idx
 ON public.peptide_instance_peptide_match_map
 ( peptide_match_id );

CREATE INDEX peptide_instance_master_quant_component_idx
 ON public.peptide_instance
 ( master_quant_component_id );

CREATE INDEX object_tree_schema_name_idx
 ON public.object_tree
 ( schema_name );

CREATE INDEX ms_query_spectrum_idx
 ON public.ms_query
 ( spectrum_id );

CREATE INDEX master_quant_peptide_ion_best_peptide_match_idx
 ON public.master_quant_peptide_ion
 ( best_peptide_match_id );

CREATE INDEX peptide_instance_best_peptide_match_idx
 ON public.peptide_instance
 ( best_peptide_match_id );

CREATE INDEX sequence_match_best_peptide_match_idx
 ON public.sequence_match
 ( best_peptide_match_id );

CREATE INDEX master_quant_peptide_ion_master_quant_component_idx
 ON public.master_quant_peptide_ion
 ( master_quant_component_id );

CREATE INDEX peptide_match_relation_child_peptide_match_idx
 ON public.peptide_match_relation
 ( child_peptide_match_id );

ALTER TABLE public.protein_set ADD CONSTRAINT protein_match_protein_set_fk
FOREIGN KEY (representative_protein_match_id)
REFERENCES public.protein_match (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

/* END OF SCRIPT GENERATED BY POWER ARCHITECT AND MODIFIED MANUALLY */


/* ADDITIONAL SQL QUERIES FIXING THE "ON DELETE CASCADE" CONSTRAINTS" */

ALTER TABLE public.peptide_readable_ptm_string DROP CONSTRAINT result_set_peptide_readable_ptm_string_fk;
ALTER TABLE public.peptide_readable_ptm_string ADD CONSTRAINT result_set_peptide_readable_ptm_string_fk
FOREIGN KEY (result_set_id)
REFERENCES public.result_set (id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.protein_set DROP CONSTRAINT result_summary_protein_set_fk;
ALTER TABLE public.protein_set ADD CONSTRAINT result_summary_protein_set_fk
FOREIGN KEY (result_summary_id)
REFERENCES public.result_summary (id)
ON DELETE CASCADE
ON UPDATE NO ACTION
NOT DEFERRABLE;

/* END ADDITIONAL SQL QUERIES FIXING THE "ON DELETE CASCADE" CONSTRAINTS" */


/* ADDITIONAL SQL QUERIES USED FOR DATA UPDATE */

-- UPDATE peptide_set.sequence_count COLUMN value
UPDATE peptide_set SET sequence_count = pepSetCount.seqCount
from (
SELECT 
  peptide_set.id as peptideSetId, 
  count(distinct peptide.sequence) as seqCount
FROM 
  public.peptide_set, 
  public.peptide_instance, 
  public.peptide_set_peptide_instance_item, 
  public.peptide
WHERE 
  peptide_instance.peptide_id = peptide.id AND
  peptide_set_peptide_instance_item.peptide_instance_id = peptide_instance.id AND
  peptide_set_peptide_instance_item.peptide_set_id = peptide_set.id
GROUP BY peptide_set.id
) pepSetCount
WHERE id = pepSetCount.peptideSetId;

-- ENABLE NOT NULL constraint on peptide_set.sequence_count COLUMN
ALTER TABLE peptide_set ALTER COLUMN sequence_count SET NOT NULL;

-- Enforce that peaklist.raw_file_identifier does not contain file extension
UPDATE peaklist SET raw_file_identifier = split_part(raw_file_identifier,'.', 1);

SELECT setval('scoring_id_seq', (SELECT max(id) FROM scoring));
DELETE FROM scoring WHERE search_engine = 'percolator';
-- Add Missing scoring 
WITH all_values (search_engine,name,description) as (
  values 
  ('mascot', 'ions score', 'The score provided for each Mascot peptide.'),
  ('mascot', 'standard score', 'The score provided for each Mascot protein hit (it corresponds to the sum of ion scores).'),
  ('mascot', 'mudpit score', 'The score provided for each Mascot protein hit when the number of MS/MS queries is high.'),
  ('mascot', 'modified mudpit score', 'A modified version of the MudPIT score computed by Proline.'),
  ('omssa', 'expect value', 'The -log(E-value) provided by OMSSA for a peptide match.'), 
  ('comet', 'evalue log scaled', 'The -log(expectation value) provided by Comet for a peptide match.'),
  ('msgf', 'evalue log scaled', 'The -log(EValue) provided by MS-GF for a peptide match.'),
  ('sequest', 'expect log scaled', 'The -log(expect) provided by Sequest for a peptide match.'),
  ('xtandem', 'hyperscore', 'The hyperscore provided by X!Tandem for a peptide match.')
)

INSERT INTO scoring (search_engine,name,description)
SELECT search_engine,name,description
FROM all_values
WHERE NOT EXISTS (SELECT 1 
                  FROM scoring sc 
                  WHERE sc.name = all_values.name AND sc.search_engine = all_values.search_engine);

/* END OF ADDITIONAL SQL QUERIES USED FOR DATA UPDATE */


