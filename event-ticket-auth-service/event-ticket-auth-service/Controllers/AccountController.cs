using event_ticket_auth_service.Data;
using EventServiceAPI.Services;
using HotelServiceAPI.DTOs_POST;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;

namespace event_ticket_auth_service.Controllers
{
    [Route("[controller]")]
    [ApiController]
    public class AccountController : ControllerBase
    {
        private readonly SignInManager<IdentityUser> _signInManager;
        private readonly UserManager<IdentityUser> _userManager;
        private readonly TokenService _tokenService;
        private readonly EventDbContext _context;

        public AccountController(
            SignInManager<IdentityUser> signInManager,
            UserManager<IdentityUser> userManager,
            TokenService tokenService,
            EventDbContext context)
        {
            _signInManager = signInManager;
            _userManager = userManager;
            _tokenService = tokenService;
            _context = context;
        }

        [HttpPost("register")]
        public async Task<ActionResult> Register([FromBody] RegisterPostDTO model)
        {
            using var transaction = await _context.Database.BeginTransactionAsync();

            try
            {
                var user = new IdentityUser
                {
                    UserName = model.Email,
                    Email = model.Email
                };

                var result = await _userManager.CreateAsync(user, model.Password);

                if (!result.Succeeded)
                {
                    return BadRequest(result.Errors);
                }

                if (!string.IsNullOrEmpty(model.Role))
                {
                    await _userManager.AddToRoleAsync(user, model.Role);
                }

                await transaction.CommitAsync();

                var token = await _tokenService.CreateTokenAsync(user);

                Response.Cookies.Append("jwt", token, new CookieOptions
                {
                    HttpOnly = true,
                    Secure = false,        // Set to true in production
                    SameSite = SameSiteMode.Lax,
                    Expires = DateTime.UtcNow.AddHours(2)
                });

                return Ok(new { token }); // Remove token from response body in production
            }
            catch (Exception)
            {
                await transaction.RollbackAsync();
                throw;
            }
            
        }

        [HttpPost("login")]
        public async Task<ActionResult> Login([FromBody] LoginPostDTO model)
        {
            var user = await _userManager.FindByEmailAsync(model.Email);
            if (user == null)
                return NotFound("User with given email not found");
            if (!(await _userManager.CheckPasswordAsync(user!, model.Password)))
                return BadRequest("Email or password incorrect");

            var token = await _tokenService.CreateTokenAsync(user);

            Response.Cookies.Append("jwt", token, new CookieOptions
            {
                HttpOnly = true,
                Secure = false,        // Set to true in production
                SameSite = SameSiteMode.Lax,
                Expires = DateTime.UtcNow.AddHours(2)
            });

            return Ok(new { token }); // Remove token from response body in production
        }

        [HttpGet("me")]
        public IActionResult Me()
        {
            var token = Request.Cookies["jwt"];
            return Ok(token);
        }
    }
}
