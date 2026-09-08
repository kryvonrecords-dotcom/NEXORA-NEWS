import "dotenv/config";
import { createClient } from '@supabase/supabase-js';

const url = process.env.SUPABASE_URL;

// Compatível com as chaves novas e antigas do Supabase
const key =
  process.env.SUPABASE_SECRET_KEY ||
  process.env.SUPABASE_PUBLISHABLE_KEY ||
  process.env.SUPABASE_ANON_KEY;

if (!url || !key) {
  console.warn('Supabase não configurado.');
}

export const supabase = url && key
  ? createClient(url, key)
  : null;
