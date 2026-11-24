-- datos ficticios; seguro ejecutar varias veces
INSERT INTO pasajero (nombres, apellidos, tipo_documento, numero_documento, fecha_nacimiento, correo, telefono, nacionalidad)
VALUES
('Laura', 'Gomez', 'CC', '1000001001', '1995-04-12', 'laura.demo@example.com', '3001234567', 'Colombiana'),
('Andres', 'Ruiz', 'PAS', 'P0000002', '1988-09-23', 'andres.demo@example.com', '3109876543', 'Colombiana'),
('Sofia', 'Perez', 'CE', 'CE000003', '1999-12-01', 'sofia.demo@example.com', '3201112233', 'Mexicana')
ON CONFLICT (tipo_documento, numero_documento) DO NOTHING;
