using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using event_ticket_auth_service.Data;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;

namespace EventServiceAPI.Services
{
    public class TokenService
    {
        public readonly IConfiguration _config;
        public readonly UserManager<IdentityUser> _userManager;

        public TokenService(IConfiguration config, UserManager<IdentityUser> userManager)
        {
            _config = config;
            _userManager = userManager;
        }

        public async Task<string> CreateTokenAsync(IdentityUser user)
        {
            // 1. Zbieramy tradycyjne claimy .NET (tak jak do tej pory)
            var identityClaims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id)
            };

            var roles = await _userManager.GetRolesAsync(user);
            identityClaims.AddRange(roles.Select(r => new Claim(ClaimTypes.Role, r)));

            // 2. ELEGANCJA: Automatyczny translator długich nazw .NET na standardy JWT (sub, role)
            var jwtClaims = identityClaims.Select(c => c.Type switch
            {
                ClaimTypes.NameIdentifier => new Claim("sub", c.Value),
                ClaimTypes.Role => new Claim("role", c.Value),
                _ => c // cała reszta claimów leci bez zmian
            }).ToArray();

            // 3. Generujemy token używając już oczyszczonych claimów
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_config["JWT_KEY"]!));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
                issuer: _config["Jwt:Issuer"],
                audience: _config["Jwt:Audience"],
                claims: jwtClaims, // Przekazujemy przemapowane claimy
                expires: DateTime.UtcNow.AddHours(2),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
