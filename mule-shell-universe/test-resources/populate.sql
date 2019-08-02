ALTER TABLE public.role ALTER COLUMN id SET DEFAULT uuid_in((md5((random())::text))::cstring);

INSERT INTO public.role (created_at, key, name, priority, value) VALUES (now(), 'net.sf.zoftwhere.mule.model:ADMIN', 'ADMIN', 70, 'admin');
INSERT INTO public.role (created_at, key, name, priority, value) VALUES (now(), 'net.sf.zoftwhere.mule.model:CLIENT', 'CLIENT', 50, 'client');
INSERT INTO public.role (created_at, key, name, priority, value) VALUES (now(), 'net.sf.zoftwhere.mule.model:GUEST', 'GUEST', 10, 'guest');
INSERT INTO public.role (created_at, key, name, priority, value) VALUES (now(), 'net.sf.zoftwhere.mule.model:REGISTER', 'REGISTER', 30, 'register');
INSERT INTO public.role (created_at, key, name, priority, value) VALUES (now(), 'net.sf.zoftwhere.mule.model:SYSTEM', 'SYSTEM', 90, 'system');
