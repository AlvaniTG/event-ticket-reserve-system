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

        public AccountController(
            SignInManager<IdentityUser> signInManager,
            UserManager<IdentityUser> userManager,
            TokenService tokenService)
        {
            _signInManager = signInManager;
            _userManager = userManager;
            _tokenService = tokenService;
        }

        [HttpPost("register")]
        public async Task<ActionResult> Register([FromBody] RegisterPostDTO model)
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
