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
            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id)
            };
            var roles = await _userManager.GetRolesAsync(user);
            claims = claims.Concat(roles.Select(r => new Claim(ClaimTypes.Role, r))).ToArray();

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_config["JWT_KEY"]!));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
                issuer: _config["Jwt:Issuer"],
                audience: _config["Jwt:Audience"],
                claims: claims,
                expires: DateTime.UtcNow.AddHours(2),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
