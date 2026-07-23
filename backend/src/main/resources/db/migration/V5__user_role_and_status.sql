-- Adds the two fields needed for an admin dashboard:
--   role   - drives authorization (@PreAuthorize("hasRole('ADMIN')")) via
--            the authority JwtAuthenticationFilter now grants
--   status - lets an admin suspend an account; a suspended user is
--            rejected at login even with a correct password
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER';
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';